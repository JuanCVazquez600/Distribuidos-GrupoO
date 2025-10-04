import React, { useState, useEffect } from 'react';
import axios from 'axios';

const InventoryForm = ({ item, onSuccess, onCancel, currentUser }) => {
  const [formData, setFormData] = useState({
    id: 0,
    categoria: 0,
    descripcion: '',
    cantidad: 0,
    eliminado: false,
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (item) {
      setFormData(item);
    }
  }, [item]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : type === 'number' ? parseInt(value) : value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      const dataToSend = { ...formData, userId: currentUser?.id || 0 };
      if (item) {
        await axios.put('http://localhost:5000/donaciones', dataToSend);
      } else {
        await axios.post('http://localhost:5000/donaciones', dataToSend);
      }
      onSuccess();
    } catch (error) {
      alert('Error al guardar donación: ' + error.response?.data?.mensaje || error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const categorias = [
    { value: 0, label: '👕 Ropa', descripcion: 'Ropa, calzado, accesorios' },
    { value: 1, label: '🍕 Alimentos', descripcion: 'Alimentos no perecederos, enlatados' },
    { value: 2, label: '🧸 Juguetes', descripcion: 'Juguetes, juegos, material educativo' },
    { value: 3, label: '📚 Útiles Escolares', descripcion: 'Libros, cuadernos, material escolar' },
  ];

  const categoriaSeleccionada = categorias.find(cat => cat.value === formData.categoria);

  return (
    <form onSubmit={handleSubmit} style={{ width: '100%' }}>
      <div className="form-group">
        <label className="form-label" style={{
          display: 'flex',
          alignItems: 'center',
          gap: 'var(--spacing-sm)'
        }}>
          <span>📦</span>
          Categoría
        </label>
        <select
          name="categoria"
          value={formData.categoria}
          onChange={handleChange}
          required
          className="form-select"
          disabled={isSubmitting}
        >
          {categorias.map(cat => (
            <option key={cat.value} value={cat.value}>
              {cat.label}
            </option>
          ))}
        </select>
        {categoriaSeleccionada && (
          <small style={{
            color: 'var(--text-muted)',
            fontSize: '0.8rem',
            marginTop: 'var(--spacing-xs)',
            display: 'block'
          }}>
            💡 {categoriaSeleccionada.descripcion}
          </small>
        )}
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
          placeholder="Describe detalladamente la donación..."
          rows="3"
          disabled={isSubmitting}
        />
        <small style={{
          color: 'var(--text-muted)',
          fontSize: '0.8rem',
          marginTop: 'var(--spacing-xs)',
          display: 'block'
        }}>
          💡 Incluye detalles como talla, marca, condición, etc.
        </small>
      </div>

      <div className="form-group">
        <label className="form-label" style={{
          display: 'flex',
          alignItems: 'center',
          gap: 'var(--spacing-sm)'
        }}>
          <span>🔢</span>
          Cantidad
        </label>
        <input
          type="number"
          name="cantidad"
          value={formData.cantidad}
          onChange={handleChange}
          required
          className="form-input"
          min="1"
          placeholder="1"
          disabled={isSubmitting}
        />
        <small style={{
          color: 'var(--text-muted)',
          fontSize: '0.8rem',
          marginTop: 'var(--spacing-xs)',
          display: 'block'
        }}>
          📊 Cantidad de unidades disponibles
        </small>
      </div>

      <div className="form-group">
        <div className="checkbox-group" style={{
          background: 'linear-gradient(135deg, var(--bg-secondary) 0%, rgba(239, 68, 68, 0.05) 100%)',
          padding: 'var(--spacing-lg)',
          borderRadius: 'var(--border-radius-md)',
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
            background: 'radial-gradient(circle, rgba(239, 68, 68, 0.1) 0%, transparent 70%)',
            borderRadius: '50%',
            pointerEvents: 'none'
          }}></div>

          <div style={{ position: 'relative', zIndex: 1 }}>
            <input
              type="checkbox"
              name="eliminado"
              checked={formData.eliminado}
              onChange={handleChange}
              id="eliminado"
              disabled={isSubmitting}
            />
            <label htmlFor="eliminado" style={{
              margin: 0,
              cursor: 'pointer',
              fontWeight: '500',
              display: 'flex',
              alignItems: 'center',
              gap: 'var(--spacing-sm)'
            }}>
              <span>❌</span>
              Marcar como eliminado/retirado
            </label>
            <small style={{
              color: 'var(--text-muted)',
              fontSize: '0.8rem',
              marginTop: 'var(--spacing-xs)',
              display: 'block',
              marginLeft: 'calc(1.2rem + var(--spacing-sm))'
            }}>
              ⚠️ Esto ocultará la donación del inventario disponible
            </small>
          </div>
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
              💾 {item ? 'Actualizar' : 'Crear'} Donación
            </>
          )}
        </button>
      </div>
    </form>
  );
};

export default InventoryForm;
