import akka.actor.{Actor, ActorSystem, ActorRef, Props, ActorLogging}
import akka.event.Logging
import scala.collection.mutable.{MutableList,HashMap,ListBuffer}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

class GroupMap extends HashMap[BigInt, List[ActorRef]]
case class Multicast() 

class GroupServer (val myNodeID: Int, val numNodes: Int, val numGroups: Int, storeServers: Seq[ActorRef], burstSize: Int) extends Actor {
  val generator = new scala.util.Random
  val cellstore = new KVClient(storeServers)
  val localWeight: Int = 90
  val log = Logging(context.system, this)

  var stats = new Stats
  var groups: ListBuffer[BigInt] = new ListBuffer[BigInt]
  var endpoints: Option[Seq[ActorRef]] = None

  val cluster = Cluster(context.system)
  
  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberEvent], classOf[UnreachableMember])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive() = {
    case Prime() =>
      allocGroup
    case Command() =>
      statistics(sender)
      command
    case View(e) =>
      endpoints = Some(e)
    case Multicast() =>
      stats.castIn += 1
      
  }

  private def command() = {
    val sample = generator.nextInt(100)
    if (sample % 2 == 0) {
      joinGroup
    } else if(sample % 5 == 0) {
      leaveGroup
    } else {
      sendMulticast
    }
  }

  private def statistics(master: ActorRef) = {
    stats.messages += 1
    if (stats.messages >= burstSize) {
      master ! BurstAck(myNodeID, stats)
      stats = new Stats
    }
  }

  private def allocGroup() = { 
    val key = chooseEmptyCell
    var cell = directRead(key)
    assert(cell.isEmpty)
    val r : List[ActorRef] = Nil
    stats.allocated += 1
    directWrite(key, r)
  }

  private def joinGroup() = {
    var groupkey = chooseActiveCell
    if (!groups.contains(groupkey)) {
      var cell = directRead(groupkey) 
      if (cell.isEmpty) {
        stats.misses += 1
      } else {
        var members = cell.get
        members = self :: members
        stats.joined += 1
        groups += groupkey
        directWrite(groupkey,members)
      }
    }
  }

  private def leaveGroup() = {
    var groupkey = chooseActiveCell
    if (groups.contains(groupkey)) {
      var cell = directRead(groupkey)
      if (cell.isEmpty) {
        stats.misses += 1
      } else {
        var members = cell.get
        members = members.filter(_ == self)
        stats.left += 1
        groups -= groupkey
        directWrite(groupkey,members)
      }
    }
  }
  
  private def sendMulticast() = {
    var groupkey = chooseActiveCell
    if (groups.contains(groupkey)) {
      var cell = directRead(groupkey)
      if (cell.isEmpty) {
        stats.misses += 1
      } else {
        var members = cell.get
        for( m <- members ){
          m ! Multicast()
          stats.castOut += 1
        }
      }
    }
  }

  private def chooseEmptyCell(): BigInt =
  {
    
    cellstore.hashForKey(myNodeID,1)
  }

  private def chooseActiveCell(): BigInt = { 
    val chosenNodeID =
      if (generator.nextInt(100) <= localWeight)
        myNodeID
      else
        generator.nextInt(numNodes - 1)

    
    cellstore.hashForKey(chosenNodeID, 1)
  }

  private def rwcheck(key: BigInt, value: List[ActorRef]) = { 
    directWrite(key, value)
    val returned = read(key)
    if (returned.isEmpty)
      println("rwcheck failed: empty read")
    else
      println("rwcheck succeeded")
  }

  private def read(key: BigInt): Option[List[ActorRef]] = {
    val result = cellstore.read(key)
    if (result.isEmpty) None else
      Some(result.get.asInstanceOf[List[ActorRef]])
  }

  private def write(key: BigInt, value: List[ActorRef], dirtyset: AnyMap): Option[List[ActorRef]] = {
    val coercedMap: AnyMap = dirtyset.asInstanceOf[AnyMap]
    val result = cellstore.write(key, value, coercedMap)
    if (result.isEmpty) None else
      Some(result.get.asInstanceOf[List[ActorRef]])
  }
  private def directRead(key: BigInt): Option[List[ActorRef]] = {
    val result = cellstore.directRead(key)
    if (result.isEmpty) None else
      Some(result.get.asInstanceOf[List[ActorRef]])
  }

  private def directWrite(key: BigInt, value: List[ActorRef]): Option[List[ActorRef]] = {
    val result = cellstore.directWrite(key, value)
    if (result.isEmpty) None else 
      Some(result.get.asInstanceOf[List[ActorRef]])
  }

}

object GroupServer {
  def props(myNodeID: Int, numNodes: Int, numGroups: Int, storeServers: Seq[ActorRef], burstSize: Int): Props = {
    Props(classOf[GroupServer], myNodeID, numNodes, numGroups, storeServers, burstSize)
  }
}