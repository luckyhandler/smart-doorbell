package de.handler.mobile.smartdoorbell

data class DoorbellItem(var timestamp: Long? = 0L,
                        var image: String? = "",
                        var annotations: Map<String, Float>? = null)