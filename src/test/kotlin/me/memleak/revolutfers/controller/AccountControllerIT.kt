package me.memleak.revolutfers.controller

import com.google.inject.Guice
import junit.framework.TestCase
import me.memleak.revolutfers.ServerStartup
import me.memleak.revolutfers.guicemodule.MyGuiceModule

class AccountControllerIT : TestCase() {
  private val port = 8000
  private val url = "http://localhost:8000/"

  private lateinit var server: ServerStartup

  override fun setUp() {
    server = Guice.createInjector(MyGuiceModule()).getInstance(ServerStartup::class.java)
      .boot(port)
  }

  override fun tearDown() {
    server.shutdown()
  }
}