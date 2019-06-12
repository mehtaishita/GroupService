class Stats {
  var messages: Int = 0 // messages received from master
  var joined: Int = 0 // groups joined
  var left: Int = 0 // groups left
  var multicasted: Int = 0 // times messages multicasted to group members
  var misses: Int = 0 //trying to leave a group but not there 
  var smirks: Int = 0 //got a message from a group you already left
  var errors: Int = 0

  def += (right: Stats): Stats = {
    messages += right.messages
    joined += right.joined
    left += right.left
    multicasted += right.multicasted
    smirks += right.smirks
    misses += right.misses
    errors += right.errors
    this
  }

  override def toString(): String = {
    s"Stats msgs=$messages alloc=$allocated checks=$checks touches=$touches miss=$misses err=$errors"
  }
}
