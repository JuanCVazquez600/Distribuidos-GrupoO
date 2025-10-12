import React, { useState } from 'react';
import axios from 'axios';

const ExternalEventForm = ({ event, onSuccess, onCancel, currentUser }) => {
  const [formData, setFormData] = useState({
    eventId: '',
    name: '',
    description: '',
    date: '',
    organizationId: 'org-300' // Fixed from backend
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      const eventData = {
        organizationId: formData.organizationId,
        eventId: `event-${Date.now()}`,
        eventName: formData.name,
        description: formData.description,
        dateTime: formData.date + ":00"
      };

      await axios.post('http://localhost:5000/events/publish', eventData);
      onSuccess();
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
      alert('Error al publicar evento: ' + errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ width: '100%' }}>
      <div style={{ marginBottom: 'var(--spacing-lg)' }}>
        <h3 style={{ marginBottom: 'var(--spacing-md)', color: 'var(--text-primary)' }}>
          📅 Publicar Evento Externo
        </h3>
        <p style={{ color: 'var(--text-secondary)', marginBottom: 'var(--spacing-lg)' }}>
          Crea un evento solidario abierto a miembros de otras organizaciones.
        </p>
      </div>

      <div className="form-group">
        <label className="form-label" style={{
          display: 'flex',
          alignItems: 'center',
          gap: 'var(--spacing-sm)'
        }}>
          <span>📅</span>
          Nombre del Evento
        </label>
        <input
          type="text"
          name="name"
          value={formData.name}
          onChange={handleChange}
          required
          className="form-input"
          placeholder="Ej: Jornada Solidaria 2024"
          disabled={isSubmitting}
        />
      </div>

      <div className="form-group">
        <label className="form-label" style={{
          display: 'flex',
          alignItems: 'center',
          gap: 'var(--spacing-sm)'
        }}>
          <span>📝</span>
          Descripción
        </label>
        <textarea
          name="description"
          value={formData.description}
          onChange={handleChange}
          required
          className="form-textarea"
          placeholder="Describe el propósito, actividades y objetivos del evento..."
          rows="4"
          disabled={isSubmitting}
        />
        <small style={{
          color: 'var(--text-muted)',
          fontSize: '0.8rem',
          marginTop: 'var(--spacing-xs)',
          display: 'block'
        }}>
          💡 Proporciona detalles claros para que los participantes sepan qué esperar
        </small>
      </div>

      <div className="form-group">
        <label className="form-label" style={{
          display: 'flex',
          alignItems: 'center',
          gap: 'var(--spacing-sm)'
        }}>
          <span>🕒</span>
          Fecha y Hora
        </label>
        <input
          type="datetime-local"
          name="date"
          value={formData.date}
          onChange={handleChange}
          required
          className="form-input"
          disabled={isSubmitting}
        />
        <small style={{
          color: 'var(--text-muted)',
          fontSize: '0.8rem',
          marginTop: 'var(--spacing-xs)',
          display: 'block'
        }}>
          📅 Selecciona la fecha y hora en que se realizará el evento
        </small>
      </div>

      <div style={{
        background: 'linear-gradient(135deg, var(--bg-secondary) 0%, rgba(6, 182, 212, 0.05) 100%)',
        padding: 'var(--spacing-lg)',
        borderRadius: 'var(--border-radius-md)',
        marginBottom: 'var(--spacing-lg)',
        border: '1px solid var(--border-light)',
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
          <h4 style={{
            margin: '0 0 var(--spacing-sm) 0',
            color: 'var(--text-primary)',
            fontSize: '1rem',
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--spacing-sm)'
          }}>
            <span>👥</span>
            Evento Externo
          </h4>
          <p style={{
            margin: 0,
            fontSize: '0.9rem',
            color: 'var(--text-secondary)',
            lineHeight: '1.5'
          }}>
            Este evento será visible para miembros de otras organizaciones, quienes podrán adherirse.
            Los miembros se gestionarán automáticamente a través del sistema de adhesiones.
          </p>
        </div>
      </div>

      <div style={{
        display: 'flex',
        gap: 'var(--spacing-md)',
        justifyContent: 'flex-end',
        marginTop: 'var(--spacing-2xl)',
        paddingTop: 'var(--spacing-lg)',
        borderTop: '1px solid var(--border-light)'
      }}>
        <button
          type="button"
          className="btn btn-secondary"
          onClick={onCancel}
          disabled={isSubmitting}
        >
          ❌ Cancelar
        </button>
        <button
          type="submit"
          className="btn btn-primary"
          disabled={isSubmitting}
        >
          {isSubmitting ? (
            <>
              <div className="spinner"></div>
              Publicando...
            </>
          ) : (
            <>
              📤 Publicar Evento
            </>
          )}
        </button>
      </div>
    </form>
  );
};

export default ExternalEventForm;
