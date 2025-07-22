package org.util

class Shipment(status:String, public val id: String, expectedDeliveryDateTimestamp: Long = 0, currentLocation: String= ""): Subject<ShipmnetObserver>{

    private val _notes: MutableList<String> = mutableListOf()
    val notes
        get() = _notes.map { it }.toMutableList()
    private val _updateHistory: MutableList<StatusChange> = mutableListOf()
    val updateHistory: MutableList<StatusChange>
        get() = _updateHistory.map { it }.toMutableList()

    private val observers = mutableListOf<ShipmnetObserver>()

    public var currentLocation: String = currentLocation
        set(location){
            field = location
            this.notifyObservers()
        }

    public var status:String = status
        set(status){
            field = status
            this.notifyObservers()
        }

    public var expectedDeliveryDateTimestamp: Long = expectedDeliveryDateTimestamp
        set(date){
            field = date
            this.notifyObservers()
        }

    public fun addNote(note: String){
        _notes.add(note)
        this.notifyObservers()
    }

    public fun addStatus(status: String, timeStamp: Long){
        _updateHistory.add(StatusChange(this.status, status, timeStamp))
        this.status = status
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

}