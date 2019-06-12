import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps
import com.typesafe.config.ConfigFactory

import akka.actor.{ActorSystem}
import akka.pattern.ask
import akka.util.Timeout

object TestHarness {
  implicit val timeout = Timeout(60 seconds)
  val numNodes = 10
  val burstSize = 1000
  val opsPerNode = 10000

  // Service tier: create app servers and a Seq of per-node Stats
 
  
  def main(args: Array[String]): Unit =  {
    startup()
    run()
  }

  def startup() : Unit = {}
    
    val port = "2551"

      val config = ConfigFactory.parseString(s"""
          akka.remote.netty.tcp.port=$port
        """).withFallback(ConfigFactory.load())

      val system = ActorSystem("GroupService",config)
  
      val master = KVAppService(system, numNodes, burstSize)
  

  def run(): Unit = {
    val s = System.currentTimeMillis
    runUntilDone
    val runtime = System.currentTimeMillis - s
    val throughput = (opsPerNode * numNodes)/runtime
    println(s"Done in $runtime ms ($throughput Kops/sec)")
    system.terminate
  }

  def runUntilDone() = {
    
      master ! Start(opsPerNode)
      val future = ask(master, Join()).mapTo[Stats]
      val done = Await.result(future, 60 seconds)
    
  }
}
