package com.example.btarduino.app;

/**
* Created by witchraper on 24/01/15.
*/
public class Sensor {

private String informacion = "";
private String temperatura = "";
private String luminosidad = "";

public String getInformacion() {
return informacion;
}

public void setInformacion(String informacion) {
this.informacion = informacion;
}

public String getTemperatura() {
return temperatura;
}

public void setTemperatura(String temperatura) {
this.temperatura = temperatura;
}

public String getLuminosidad() {
return luminosidad;
}

public void setLuminosidad(String luminosidad) {
	this.luminosidad = luminosidad;
}
}
