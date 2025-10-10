package Distribuidos_GrupoO.ServidorGRPC.service.kafka.adhesion;

public class EventAdhesion {
    private String eventId;
    private String idOrganizacion;
    private String idVoluntario;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;

    public EventAdhesion() {}

    public EventAdhesion(String eventId, String idOrganizacion, String idVoluntario, 
                        String nombre, String apellido, String telefono, String email) {
        this.eventId = eventId;
        this.idOrganizacion = idOrganizacion;
        this.idVoluntario = idVoluntario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.email = email;
    }

    // Getters y Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getIdOrganizacion() {
        return idOrganizacion;
    }

    public void setIdOrganizacion(String idOrganizacion) {
        this.idOrganizacion = idOrganizacion;
    }

    public String getIdVoluntario() {
        return idVoluntario;
    }

    public void setIdVoluntario(String idVoluntario) {
        this.idVoluntario = idVoluntario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "EventAdhesion{" +
                "eventId='" + eventId + '\'' +
                ", idOrganizacion='" + idOrganizacion + '\'' +
                ", idVoluntario='" + idVoluntario + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}