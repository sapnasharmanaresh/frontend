package test

import play.api.test._
import play.api.test.Helpers._
import org.scalatest.Matchers
import org.scalatest.FlatSpec
import common.UsesElasticSearch

class ArchiveControllerTest extends FlatSpec with Matchers with UsesElasticSearch {
  
 
  "Archive Controller" should "return a HTTP 303 when it finds a match in DynamoDB" in Fake {
    val url = "www.theguardian.com/media/emailservices/article/0,,1694396,.html"
    val result = controllers.ArchiveController.lookup(url)(TestRequest())
    header("Location", result) should be(Some("http://www.theguardian.com/media/2006/jan/25/1"))
    status(result) should be(303)
  }
  
  it should "return a HTTP 200 when it finds a resource in S3" in Fake {
    val url = "travel.theguardian.com/askatraveller/0,,345059,00.html"
    val result = controllers.ArchiveController.lookup(url)(TestRequest())
    status(result) should be(200)
    contentType(result) should be(Some("text/html"))
  }

  it should "return a HTTP 404 when it can not find a resource" in Fake {
    val url = "www.theguardian.com/i/am/not/here"
    val result = controllers.ArchiveController.lookup(url)(TestRequest())
    status(result) should be(404)
  }

  it should "fetch the HTML of a valid archived URL" in Fake {
    val result = controllers.ArchiveController.isArchived("travel.theguardian.com/askatraveller/0,,345059,00.html")
    result.get should include ("<title>Ask a traveller | guardian.co.uk Travel</title>")
  }
  
  it should "return None for a invalid archive URL" in Fake {
    val result = controllers.ArchiveController.isArchived("not/here")
    result should be(None)
  }
  
  it should "fetch the destination of a valid redirected URL" in Fake {
    val result = controllers.ArchiveController.isRedirect("www.theguardian.com/media/emailservices/article/0,,1694396,.html")
    result should be (Some("http://www.theguardian.com/media/2006/jan/25/1"))
  }
  
  it should "return Noen for a path that has not been redirected" in Fake {
    val result = controllers.ArchiveController.isRedirect("not/here")
    result should be(None)
  }

}