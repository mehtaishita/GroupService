class Stats {
  var messages: Int = 0 // messages received from master
  var allocated: Int = 0 //groups allocated
  var joined: Int = 0 // groups joined
  var left: Int = 0 // groups left
  var castOut: Int = 0 // times messages multicasted to group members
  var castIn: Int = 0 //times messages received from group members
  var misses: Int = 0 //group doesn't exist in the store
  var smirks: Int = 0 //got a message from a group you already left
  var errors: Int = 0 

  def += (right: Stats): Stats = {
    messages += right.messages
    allocated += right.allocated
    joined += right.joined
    left += right.left
    castOut += right.castOut
    castIn += right.castIn
    smirks += right.smirks
    misses += right.misses
    errors += right.errors
    this
  }

  override def toString(): String = {
    s"Stats msgs=$messages alloc=$allocated joined=$joined left=$left sent=$castOut recd=$castIn miss=$misses skrt=$smirks err=$errors"
  }
}
