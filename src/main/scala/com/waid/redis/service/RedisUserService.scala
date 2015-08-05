package com.waid.redis.service

import com.waid.redis.model.{UserStreamNode, Node, UserTokenNode, UserNode}
import com.waid.redis.utils.RedisUtils
import com.waid.redis.{KeyPrefixGenerator, RedisDataStore, RedisReadOperations}

/**
 * Created by kodjobaah on 16/07/2015.
 */
object RedisUserService {

  def removeForgottenPassword(email: Option[String]) = {

    for(e <- email) {
      RedisDataStore.removeForgottenPassword(e)
    }
  }


  def removePreviousValidLoginDetails(email: String) = {

    var authToken = RedisReadOperations.getUserAuthTokenId(email)

    for(at <- authToken) {
      RedisDataStore.removeElement(KeyPrefixGenerator.LookupValidLogins, at)
      RedisDataStore.removeElement(KeyPrefixGenerator.LookupValidLoginsEmail, email)

    }
  }

  def removeValidStreams(email: String) = {
    RedisDataStore.removeValidStreams(email)
  }
  def createStream(authToken: String): Option[UserStreamNode] = {

      var userNode = RedisReadOperations.getUserForAuthToken(authToken)
      var userStreamNode: Option[UserStreamNode] = None
      var userTokenNode = RedisReadOperations.getUserTokenNodeId(authToken)
      for (un <- userNode; userTokenNodeId <- userTokenNode) {

          var email = un.attributes get KeyPrefixGenerator.Email
          userStreamNode = Some(RedisDataStore.createStreamForUser(userTokenNodeId,email))

      }
        userStreamNode
  }

  def checkIfTokenIsValid(token: String):Option[UserNode] = {
      RedisReadOperations.getUserForAuthToken(token)
  }

  def checkForgottonPassword(em: String, changePasswordId: String): Option[UserNode] = {
    val token = RedisReadOperations.getForgottenPasswordToken(em)
    var user: Option[UserNode] = None
    for(tk <- token) {

      if (tk.equals(changePasswordId))
          user = RedisReadOperations.getUser(em)
    }
    user
  }

  def setForgottonPassword(em: String): String = {

    val token = RedisUtils.getUUID().toString
    RedisDataStore.addForgottonPassword(em,token)
    token
  }
  def removeCurrentToken(em: String) = {
    RedisDataStore.removeCurrentToken(em)

  }

  def createToken(userNode: Option[UserNode]): UserTokenNode = {

    var userToken:Option[UserTokenNode] = None
    for(user <- userNode) {
      userToken = Some(RedisDataStore.createTokenForUser(user))
      var email = user.attributes get KeyPrefixGenerator.Email
      RedisDataStore.addAuthenticationToken(userToken.get,email)
    }
    userToken.get
  }

  def removeRegistrationDetails(registrationId: Option[String]) = {
      RedisDataStore.removeRegistration(registrationId.get)

  }

  def getUserFromRegistration(regToken: Option[String]): Option[UserNode] = {
    RedisReadOperations.getUserUsingRegistrationToken(regToken.get)
  }


  def findUser(email:String): Option[UserNode] = {
    RedisReadOperations.getUser(email)
  }

  def createNewUser(em:String, fn: String, ln: String, p: String): UserNode = {
    var userNode = RedisModelService.createUser(em,Some(fn),Some(ln),Some(p))
    userNode = RedisDataStore.addUser(userNode)
    RedisDataStore.createUserLookupAfterRegistration(userNode)
    userNode
  }

  def getAttributeFromNode(node: Node, att: String): Option[String] = {
      val atts = node.attributes.get
      val value = atts.get(att)
      atts.get(att)
  }
}
