import React, { useEffect, useState } from 'react';
import axios from 'axios';

const ExternalEventsList = ({ currentUser }) => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [joining, setJoining] = useState(null);
  const [deleting, setDeleting] = useState(null);

  useEffect(() => {
    fetchEvents();
  }, []);

  const fetchEvents = async () => {
    try {
      const response = await axios.get('http://localhost:5000/events/external');
      setEvents(response.data);
    } catch (error) {
      console.error('Error fetching external events:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleJoinEvent = async (event) => {
    setJoining(event.eventId);
    try {
      const joinData = {
        idVoluntario: currentUser.id,
        nombre: currentUser.nombre,
        apellido: currentUser.apellido,
        telefono: currentUser.telefono,
        email: currentUser.email
      };

      await axios.post(`http://localhost:5000/adhesions/join/${event.eventId}`, joinData);
      alert('Adhesión enviada correctamente. El organizador será notificado.');
      // Refresh events to update status
      fetchEvents();
    } catch (error) {
      let errorMessage = 'Error desconocido';
      if (error.response?.data) {
        if (typeof error.response.data === 'string') {
          errorMessage = error.response.data;
        } else if (error.response.data.error) {
          errorMessage = error.response.data.error;
        } else if (error.response.data.message) {
          errorMessage = error.response.data.message;
        } else {
          errorMessage = JSON.stringify(error.response.data);
        }
      } else {
        errorMessage = error.message;
      }
      alert('Error al adherirse al evento: ' + errorMessage);
    } finally {
      setJoining(null);
    }
  };

  const handleDeleteEvent = async (event) => {
    if (!window.confirm('¿Estás seguro de que quieres eliminar este evento externo?')) {
      return;
    }
    setDeleting(event.eventId);
    try {
      await axios.delete(`http://localhost:5000/events/external/${event.eventId}?userId=${currentUser.id}`);
      alert('Evento eliminado correctamente.');
      fetchEvents();
    } catch (error) {
      let errorMessage = 'Error desconocido';
      if (error.response?.data) {
        if (typeof error.response.data === 'string') {
          errorMessage = error.response.data;
        } else if (error.response.data.error) {
          errorMessage = error.response.data.error;
        } else if (error.response.data.message) {
          errorMessage = error.response.data.message;
        } else {
          errorMessage = JSON.stringify(error.response.data);
        }
      } else {
        errorMessage = error.message;
      }
      alert('Error al cancelar el evento: ' + errorMessage);
    } finally {
      setDeleting(null);
    }
  };

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        padding: 'var(--spacing-2xl)'
      }}>
        <div className="spinner" style={{ width: '2rem', height: '2rem' }}></div>
        <span style={{ marginLeft: 'var(--spacing-md)' }}>Cargando eventos externos...</span>
      </div>
    );
  }

  return (
    <div style={{ width: '100%' }}>
      <div style={{ marginBottom: 'var(--spacing-lg)' }}>
        <h3 style={{ marginBottom: 'var(--spacing-md)', color: 'var(--text-primary)' }}>
          📅 Eventos Externos
        </h3>
        <p style={{ color: 'var(--text-secondary)', marginBottom: 'var(--spacing-lg)' }}>
          Eventos organizados por otras ONG a los que puedes adherirte.
        </p>
      </div>

      {events.length === 0 ? (
        <div style={{
          textAlign: 'center',
          padding: 'var(--spacing-xl)',
          color: 'var(--text-muted)',
          background: 'var(--bg-secondary)',
          borderRadius: 'var(--border-radius-md)',
          border: '1px solid var(--border-light)'
        }}>
          <div style={{ fontSize: '3rem', marginBottom: 'var(--spacing-md)' }}>📅</div>
          <h4>No hay eventos externos disponibles</h4>
          <p>Los eventos de otras organizaciones aparecerán aquí cuando sean publicados.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: 'var(--spacing-lg)' }}>
          {events.map(event => (
            <div key={event.eventId} style={{
              border: '1px solid var(--border-light)',
              borderRadius: 'var(--border-radius-md)',
              padding: 'var(--spacing-lg)',
              background: 'var(--bg-secondary)',
              position: 'relative',
              overflow: 'hidden'
            }}>
              <div style={{
                position: 'absolute',
                top: '-20%',
                right: '-20%',
                width: '100px',
                height: '100px',
                background: 'radial-gradient(circle, rgba(6, 182, 212, 0.1) 0%, transparent 70%)',
                borderRadius: '50%',
                pointerEvents: 'none'
              }}></div>

              <div style={{ position: 'relative', zIndex: 1 }}>
                <div style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'flex-start',
                  marginBottom: 'var(--spacing-md)'
                }}>
                  <div style={{ flex: 1 }}>
                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 'var(--spacing-sm)',
                      marginBottom: 'var(--spacing-sm)'
                    }}>
                      <span className="category-tag">{event.organizationId}</span>
                      <h4 style={{
                        margin: 0,
                        color: 'var(--text-primary)',
                        fontSize: '1.2rem'
                      }}>
                        {event.eventName}
                      </h4>
                    </div>
                    <p style={{
                      color: 'var(--text-secondary)',
                      marginBottom: 'var(--spacing-md)',
                      lineHeight: '1.5'
                    }}>
                      {event.description}
                    </p>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)', marginLeft: 'var(--spacing-md)' }}>
                    <button
                      className="btn btn-primary btn-sm"
                      onClick={() => handleJoinEvent(event)}
                      disabled={joining === event.eventId}
                    >
                      {joining === event.eventId ? (
                        <>
                          <div className="spinner" style={{ width: '1rem', height: '1rem' }}></div>
                          Adhiriendo...
                        </>
                      ) : (
                        <>
                          🙋‍♂️ Adherirse
                        </>
                      )}
                    </button>
                    {(currentUser.rol === 'PRESIDENTE' || currentUser.rol === 'COORDINADOR') && (
                      <button
                        className="btn btn-danger btn-sm"
                        onClick={() => handleDeleteEvent(event)}
                        disabled={deleting === event.eventId}
                      >
                        {deleting === event.eventId ? (
                          <>
                            <div className="spinner" style={{ width: '1rem', height: '1rem' }}></div>
                            Eliminando...
                          </>
                        ) : (
                          <>
                            ❌ Eliminar
                          </>
                        )}
                      </button>
                    )}
                  </div>
                </div>

                <div style={{
                  display: 'grid',
                  gridTemplateColumns: '1fr 1fr',
                  gap: 'var(--spacing-lg)',
                  marginBottom: 'var(--spacing-md)'
                }}>
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 'var(--spacing-sm)',
                    color: 'var(--text-secondary)'
                  }}>
                    <span>🕒</span>
                    <span>{new Date(event.dateTime).toLocaleString()}</span>
                  </div>
                </div>

                <div style={{
                  background: 'linear-gradient(135deg, rgba(6, 182, 212, 0.05) 0%, rgba(6, 182, 212, 0.02) 100%)',
                  padding: 'var(--spacing-md)',
                  borderRadius: 'var(--border-radius-sm)',
                  border: '1px solid rgba(6, 182, 212, 0.2)'
                }}>
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 'var(--spacing-sm)',
                    fontSize: '0.9rem',
                    color: 'var(--text-secondary)'
                  }}>
                    <span>👥</span>
                    <span>Evento abierto a miembros de otras organizaciones</span>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ExternalEventsList;
