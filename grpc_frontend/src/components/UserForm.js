import React, { useState, useEffect } from 'react';
import axios from 'axios';

const UserForm = ({ user, onSuccess, onCancel, currentUser }) => {
  const [formData, setFormData] = useState({
    id: 0,
    nombreUsuario: '',
    nombre: '',
    apellido: '',
    telefono: '',
    clave: '',
    email: '',
    rol: '',
    activo: true,
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (user) {
      setFormData(user);
    }
  }, [user]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      const dataToSend = { ...formData, userId: currentUser?.id || 0 };
      if (user) {
        await axios.put('http://localhost:5000/usuarios', dataToSend);
      } else {
        await axios.post('http://localhost:5000/usuarios', dataToSend);
      }
      onSuccess();
    } catch (error) {
      alert('Error al guardar usuario: ' + error.response?.data?.mensaje || error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ width: '100%' }}>
      <div style={{
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        gap: 'var(--spacing-lg)',
        marginBottom: 'var(--spacing-lg)'
      }}>
        <div className="form-group">
          <label className="form-label" style={{
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--spacing-sm)'
          }}>
            <span>👤</span>
            Nombre de Usuario
          </label>
          <input
            type="text"
            name="nombreUsuario"
            value={formData.nombreUsuario}
            onChange={handleChange}
            required
            className="form-input"
            placeholder="Ej: juan_perez"
            disabled={isSubmitting}
          />
        </div>

        <div className="form-group">
          <label className="form-label" style={{
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--spacing-sm)'
          }}>
            <span>🏷️</span>
            Rol
          </label>
          <select
            name="rol"
            value={formData.rol}
            onChange={handleChange}
            required
            className="form-select"
            disabled={isSubmitting}
          >
            <option value="">Seleccionar rol...</option>
            <option value="PRESIDENTE">👑 Presidente</option>
            <option value="COORDINADOR">🎯 Coordinador</option>
            <option value="VOCAL">📢 Vocal</option>
            <option value="VOLUNTARIO">🤝 Voluntario</option>
          </select>
        </div>
      </div>

      <div style={{
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        gap: 'var(--spacing-lg)',
        marginBottom: 'var(--spacing-lg)'
      }}>
        <div className="form-group">
          <label className="form-label" style={{
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--spacing-sm)'
          }}>
            <span>📝</span>
            Nombre
          </label>
          <input
            type="text"
            name="nombre"
            value={formData.nombre}
            onChange={handleChange}
            required
            className="form-input"
            placeholder="Juan"
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
            Apellido
          </label>
          <input
            type="text"
            name="apellido"
            value={formData.apellido}
            onChange={handleChange}
            required
            className="form-input"
            placeholder="Pérez"
            disabled={isSubmitting}
          />
        </div>
      </div>

      <div style={{
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        gap: 'var(--spacing-lg)',
        marginBottom: 'var(--spacing-lg)'
      }}>
        <div className="form-group">
          <label className="form-label" style={{
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--spacing-sm)'
          }}>
            <span>📧</span>
            Email
          </label>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
            className="form-input"
            placeholder="juan@email.com"
            disabled={isSubmitting}
          />
        </div>

        <div className="form-group">
          <label className="form-label" style={{
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--spacing-sm)'
          }}>
            <span>📞</span>
            Teléfono
          </label>
          <input
            type="tel"
            name="telefono"
            value={formData.telefono}
            onChange={handleChange}
            className="form-input"
            placeholder="+54 11 1234-5678"
            disabled={isSubmitting}
          />
        </div>
      </div>

      {user && (
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: 'var(--spacing-md)',
          marginBottom: 'var(--spacing-lg)'
        }}>
          <input
            type="checkbox"
            name="activo"
            checked={formData.activo}
            onChange={handleChange}
            id="activo-checkbox"
            disabled={isSubmitting}
          />
          <label htmlFor="activo-checkbox" className="form-label" style={{ margin: 0 }}>
            Usuario Activo
          </label>
        </div>
      )}

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
              💾 {user ? 'Actualizar' : 'Crear'} Usuario
            </>
          )}
        </button>
      </div>
    </form>
  );
};

export default UserForm;
