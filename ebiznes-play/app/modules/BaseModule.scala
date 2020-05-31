package modules

import auth.repository.{AuthTokenRepositoryImpl, AuthTokenRepository}
import auth.services.{AuthTokenService, AuthTokenServiceImpl}
import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule

class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[AuthTokenRepository].to[AuthTokenRepositoryImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
  }
}

