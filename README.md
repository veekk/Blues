# Blues
Android bluetooth library written on Kotlin, that supports multiple connections, provides data using flexible Rx observables

# Getting started

* Create your own Data handlers by implementing the DataHandler interface. e.g:
```
class SimpleDataHandler(override val inputStream: InputStream, override val outputStream: OutputStream) : DataHandler {
    var bufferedReader = BufferedReader(InputStreamReader(inputStream))
    var bufferedWriter = BufferedWriter(OutputStreamWriter(outputStream))
    
    override fun parseData(): String = bufferedReader.readLine()
    fun write(message: String) = bufferedWriter.write("$message\r\n")
}
```

* Create your own Connection type base by implementing the BluesConnectionType interface. e.g:
```
enum class ConnectionType : BluesConnectionType {
    OBD {
        override lateinit var handler: DataHandler
        override fun createHandler(inputStream: InputStream, outputStream: OutputStream) =
                OBDDataHandler(inputStream, outputStream)
    },

    ARDUINO {
        override lateinit var handler: DataHandler
        override fun createHandler(inputStream: InputStream, outputStream: OutputStream) =
                SimpleDataHandler(inputStream, outputStream)
    },

    GPS {
        override lateinit var handler: DataHandler
        override fun createHandler(inputStream: InputStream, outputStream: OutputStream) =
                SimpleDataHandler(inputStream, outputStream)
    };
}
```

* Simply create the Blues instance providing it an application context
```
       val blues = Blues<YOUR_CONNECTION_TYPE>(context)
```
* Enable the adapter
```
       blues.enableAdapter()
```


* Subscribe to observables from blues wherever you need to get state change or data from InputStreams
```


blues.dataObservable.filter { (_, model) -> model == ConnectionType.OBD }
              .map { it.first }
              .subscribe { Log.d("TAG", it) }
```

* Connect to some devices!
```
blues.connect("aa:bb:cc:dd:ee:ff", ConnectionType.OBD)
```
