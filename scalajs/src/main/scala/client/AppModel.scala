package client


// Same as Customer service (to be move to shared ?)
import diode.Action
import upickle.default.{macroRW, ReadWriter => RW}




case class Customer (
                      trigram: String,
                      name: String,
                      customerType: String,
                      dynamicsAccountID: String,
                      headCountry: String,
                      region: String
                    )

object Customer{
  implicit def rw: RW[Customer] = macroRW
}

case class StreamForCustomer(trigram: String)
object StreamForCustomer{
  implicit def rw: RW[StreamForCustomer] = macroRW
}

sealed trait CustomerEvent extends Action
object CustomerEvent {
  import upickle.default._

  implicit val readWriter: ReadWriter[CustomerEvent] = ReadWriter.merge(
    macroRW[CustomerCreated], macroRW[CustomerRenamed]
  )
  // {"trigram":"UNO","name":"New Zeland Zero Zillion","customerType":"Operator","dynamicsAccountID":"DY-8322",
  // "headCountry":"IT","region":"EMEA","event_type":"customerCreated"}
  case class CustomerCreated (
                               trigram: String,
                               name: String,
                               customerType: String,
                               dynamicsAccountID: String,
                               headCountry: String,
                               region: String,
                               event_type: String
                             ) extends CustomerEvent

  // {"trigram":"ABC","name":"ABL","event_type":"customerRenamed"}
  case class CustomerRenamed (
                               trigram: String,
                               name: String,
                               event_type: String
                             ) extends CustomerEvent

}


case class CustomerEventType(event_type: String)
object CustomerEventType{
  implicit def rw: RW[CustomerEventType] = macroRW
}

//sealed trait CustomerEvent


// {"trigram":"ABC","name":"New Zeland Zero Zillion","customerType":"Operator","dynamicsAccountID":"DY-8322",
// "headCountry":"IT","region":"EMEA","event_type":"customerCreated"}
/*case class CustomerCreated (
                             trigram: String,
                             name: String,
                             customerType: String,
                             dynamicsAccountID: String,
                             headCountry: String,
                             region: String,
                             event_type: String
                           )
object CustomerCreated{
  implicit def rw: RW[CustomerCreated] = macroRW
}

case class CustomerRenamed (
                             trigram: String,
                             name: String,
                             event_type: String
                           )
object CustomerRenamed{
  implicit def rw: RW[CustomerRenamed] = macroRW
}*/
