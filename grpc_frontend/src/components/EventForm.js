import React, { useState, useEffect } from 'react';
import axios from 'axios';

const EventForm = ({ event, onSuccess, onCancel, currentUser }) => {
  const [formData, setFormData] = useState({
    id: 0,
    nombre: '',
    descripcion: '',
    fechaHora: '',
    miembros: [],
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (event) {
      setFormData(event);
    }
  }, [event]);

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
      const dataToSend = { ...formData, userId: currentUser?.id || 0 };
      if (event) {
        await axios.put('http://localhost:5000/eventos', dataToSend);
      } else {
        await axios.post('http://localhost:5000/eventos', dataToSend);
      }
      onSuccess();
    } catch (error) {
      alert('Error al guardar evento: ' + error.response?.data?.mensaje || error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ width: '100%' }}>
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
          name="nombre"
          value={formData.nombre}
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
          name="descripcion"
          value={formData.descripcion}
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
          name="fechaHora"
          value={formData.fechaHora}
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
            Gestión de Miembros
          </h4>
          <p style={{
            margin: 0,
            fontSize: '0.9rem',
            color: 'var(--text-secondary)',
            lineHeight: '1.5'
          }}>
            Los miembros se pueden agregar después de crear el evento desde la gestión de eventos.
            Esto permite una mejor organización y asignación de responsabilidades.
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
              Guardando...
            </>
          ) : (
            <>
              💾 {event ? 'Actualizar' : 'Crear'} Evento
            </>
          )}
        </button>
      </div>
    </form>
  );
};

export default EventForm;
