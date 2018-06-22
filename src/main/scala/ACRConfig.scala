import java.util

class ACRConfig(host: String, accessKey: String, accessSecret: String, timeout: Int, val startTime: Int)
{
  def configMap: util.HashMap[String, Object] =
  {
    val config = new util.HashMap[String, Object]()
    config.put("host", host)
    config.put("access_key", accessKey)
    config.put("access_secret", accessSecret)
    config.put("debug", false.asInstanceOf[Object])
    config.put("timeout", timeout.asInstanceOf[Object])
    config
  }

}