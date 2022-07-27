package com.wgmouton.util

import akka.actor.typed.Behavior

trait Gateway[C] {
  def apply(): Behavior[C]
}