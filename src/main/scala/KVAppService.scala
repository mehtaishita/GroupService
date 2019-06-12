import akka.actor.{ActorSystem, ActorRef, Props}
import com.typesafe.config.ConfigFactory

sealed trait AppServiceAPI
case class Prime() extends AppServiceAPI
case class Command() extends AppServiceAPI
case class View(endpoints: Seq[ActorRef]) extends AppServiceAPI

/**
 * This object instantiates the service tiers and a load-generating master, and
 * links all the actors together by passing around ActorRef references.
 *
 * The service to instantiate is bolted into the KVAppService code.  Modify this
 * object if you want to instantiate a different service.
 */

object KVAppService {

  def apply(system: ActorSystem, numNodes: Int, ackEach: Int): List[ActorRef] = {

    /** Storage tier: create K/V store servers */
    val stores = for (i <- 0 until numNodes)
      yield system.actorOf(KVStore.props(), "GenericStore" + i)

    
    /** Get the Cluster going */
    val ports = List("2551","2552","0")

    val masters = for (port <- ports) yield {
      
    
      val config = ConfigFactory.parseString(s"""
        akka.remote.netty.tcp.port=$port
        """).withFallback(ConfigFactory.load())

      val system = ActorSystem("GSClusters",config)

      /** Service tier: create app servers */
      val servers = for (i <- 0 until numNodes)
        yield system.actorOf(GroupServer.props(i, numNodes, 10, stores, ackEach), "GroupServer" + i)
       
      /** Tells each server the list of servers and their ActorRefs wrapped in a message. */
      for (server <- servers)
        server ! View(servers)

      /** Load-generating master */
      val master = system.actorOf(LoadMaster.props(numNodes, servers, ackEach), "LoadMaster")
      master

    }
    masters
  }

  
}

