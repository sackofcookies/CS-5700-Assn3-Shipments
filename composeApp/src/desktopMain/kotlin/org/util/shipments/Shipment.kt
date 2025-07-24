package org.util

abstract class Shipment(status:String, public val id: String, protected val createdDate: Long, expectedDeliveryDateTimestamp: Long, currentLocation: String): Subject<ShipmnetObserver>{

    private val _notes: MutableList<String> = mutableListOf()
    val notes
        get() = _notes.map { it }.toMutableList()
    private val _updateHistory: MutableList<StatusChange> = mutableListOf()
    val updateHistory: MutableList<StatusChange>
        get() = _updateHistory.map { it }.toMutableList()

    private val observers = mutableListOf<ShipmnetObserver>()

    abstract val type: Type


    public var currentLocation: String = currentLocation
        set(location){
            field = location
            this.verifyConditions()
            this.notifyObservers()
        }

    public var status:String = status
        set(status){
            field = status
            this.verifyConditions()
            this.notifyObservers()
        }

    public var expectedDeliveryDateTimestamp: Long = expectedDeliveryDateTimestamp
        set(date){
            field = date
            this.verifyConditions()
            this.notifyObservers()
        }

    public fun addNote(note: String){
        _notes.add(note)
        this.verifyConditions()
        this.notifyObservers()
    }

    public fun addStatus(status: String, timeStamp: Long){
        _updateHistory.add(StatusChange(this.status, status, timeStamp))
        this.status = status
        this.verifyConditions()
        this.notifyObservers()
    }

    override fun unregisterObserver(observer: ShipmnetObserver){
        observers.remove(observer)
    }
    override fun registerObserver(observer: ShipmnetObserver){
        observers.add(observer)
    }
    override fun notifyObservers(){
        observers.forEach { it.update(this) }
    }

    abstract protected fun conditions(): String?

    private fun verifyConditions(){
        val note = conditions()
        if (note != null){
            this.addNote(note)
        }
    }

    enum class Type {
        BULK,
        EXPRESS,
        OVERNIGHT,
        STANDARD
    }

}