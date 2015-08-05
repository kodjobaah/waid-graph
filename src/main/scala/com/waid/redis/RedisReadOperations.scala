package com.waid.redis

import com.redis.RedisClient
import com.waid.redis.model.UserNode
import com.waid.redis.utils.RedisUtils

/**
 * Created by kodjobaah on 16/07/2015.
 */
object RedisReadOperations {

  import RedisDataStore.clients

  def getUserAuthTokenId(email: String): Option[String] = {
    var node: Option[String] = None
    clients.withClient {
      client =>
        node = client.hget(KeyPrefixGenerator.LookupValidLoginsEmail,email)
    }
    node
  }

  def getUserTokenNodeId(token: String): Option[String] = {
    var node: Option[String] = None
    clients.withClient {
      client =>
        node = client.hget(KeyPrefixGenerator.LookupValidLogins,token)
    }
    node
  }

  def getUserForAuthToken(token: String): Option[UserNode] = {
    var user:Option[UserNode] = None
    clients.withClient {
      client =>
          var userTokenId = client.hget(KeyPrefixGenerator.LookupValidLogins,token)
          for(tokenId <- userTokenId) {
            var userNodeId = Option(RedisUtils.getUserNodeFromUserToken(tokenId))
            user = populateUserNode(client,userNodeId)
          }
    }
    user
  }

  def getUserUsingRegistrationToken(regToken: String): Option[UserNode]= {
    var user:Option[UserNode] = None
    clients.withClient{
      client =>
        val res = client.hget(KeyPrefixGenerator.LookupRegistration,regToken)
        populateUserNode(client,res)
    }
  }

  def getForgottenPasswordToken(em: String) : Option[String] = {

    var token:Option[String] = None
    clients.withClient {
      client =>
        token = client.hget(KeyPrefixGenerator.LookupLostPassword,em)
    }
    token
  }

  def getUser(email: String): Option[UserNode] = {
    var user:Option[UserNode] = None
    clients.withClient{
      client =>
        val res= client.hget(KeyPrefixGenerator.LookupUser,email)
        user = populateUserNode(client,res)
    }
    user
  }

  private def populateUserNode(client: RedisClient, userNode: Option[String]):Option[UserNode] =  {
    var user:Option[UserNode] = None
    for(un <- userNode) {
        val userNodeMap = client.hgetall(un)
        val tokens = un.split(":_")
        val id = Some(tokens(1).toLong)
        user = Some(UserNode(KeyPrefixGenerator.UserNodePrefix,id,userNodeMap))
    }
    user
  }
}
