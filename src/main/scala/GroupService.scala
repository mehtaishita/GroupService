import akka.actor.{Actor, ActorSystem, ActorRef, Props}
import akka.event.Logging

// Define a new data structure to store in the KVStore to represent the membership list. 
class GroupMap extends scala.collection.mutable.HashMap[BigInt, List[ActorRef]]

class GroupServer (val myNodeID: Int, val numNodes: Int, val numGroups: Int, storeServers: Seq[ActorRef], burstSize: Int) extends Actor {
  val generator = new scala.util.Random
  val cellstore = new KVClient(storeServers)
  val localWeight: Int = 70
  val log = Logging(context.system, this)

  var stats = new stats
  var GroupID: List[BigInt]
  var endpoints: Option[Seq[ActorRef]] = numNodes

  def receive() = {
    case Prime() =>
      allocGroup
    case Command() =>
      statistics(sender)
      command
    case View(e) =>
      endpoints = Some(e)
  }

  private def command() = {
    val sample = generator.nextInt(100)
    if (sample mod 3 == 1) {
      joinGroup
    } else if(sample mod 3 == 2) {
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

  }

  private def joinGroup() = {

  }

  private def leaveGroup() = {

  }
  
  private def sendMulticast() = {

  }

  private def rwcheck(key: BigInt, value: __) = { 
    directWrite(key, value)
    val returned = read(key)
    if (returned.isEmpty)
      println("rwcheck failed: empty read")
    else if (returned.get.next != value.next)
      println("rwcheck failed: next match")
    else if (returned.get.prev != value.prev)
      println("rwcheck failed: prev match")
    else
      println("rwcheck succeeded")
  }

  private def directRead(key: BigInt): Option[__] = {
    val result = cellstore.directRead(key)
    if (result.isEmpty) None else
      Some(result.get.asInstanceOf[___])
  }

  private def directWrite(key: BigInt, value: __): Option[__] = {
    val result = cellstore.directWrite(key, value)
    if (result.isEmpty) None else 
      Some(result.get.asInstanceOf[__])
  }


}

object GroupServer {
  def props(myNodeID: Int, numNodes: Int, numGroups: Int, storeServers: Seq[ActorRef], burstSize: Int): Props = {
    Props(classOf[GroupServer], myNodeID, numNodes, numGroups, storeServers, burstSize)
  }
}