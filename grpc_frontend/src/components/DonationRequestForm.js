import React, { useState } from 'react';
import axios from 'axios';

const DonationRequestForm = ({ onSuccess, onCancel, currentUser }) => {
  const [items, setItems] = useState([{ category: '', description: '', quantity: 1 }]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleAddItem = () => {
    setItems([...items, { category: '', description: '', quantity: 1 }]);
  };

  const handleRemoveItem = (index) => {
    setItems(items.filter((_, i) => i !== index));
  };

  const handleItemChange = (index, field, value) => {
    const newItems = [...items];
    newItems[index][field] = value;
    setItems(newItems);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      const requestData = {
        requestId: `req-${Date.now()}`,
        organizationId: 'org-300', // From backend config
        requestedDonations: items.map(item => ({
          category: item.category,
          description: item.description
        }))
      };

      await axios.post('http://localhost:5000/requests/publish', requestData);
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
      alert('Error al publicar solicitud: ' + errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const categorias = [
    { value: 'ROPA', label: '👕 Ropa' },
    { value: 'ALIMENTOS', label: '🍕 Alimentos' },
    { value: 'JUGUETES', label: '🧸 Juguetes' },
    { value: 'UTILES_ESCOLARES', label: '📚 Útiles Escolares' }
  ];

  return (
    <form onSubmit={handleSubmit} style={{ width: '100%' }}>
      <div style={{ marginBottom: 'var(--spacing-lg)' }}>
        <h3 style={{ marginBottom: 'var(--spacing-md)', color: 'var(--text-primary)' }}>
          📦 Solicitar Donaciones
        </h3>
        <p style={{ color: 'var(--text-secondary)', marginBottom: 'var(--spacing-lg)' }}>
          Agrega los items que necesitas para tu organización. Esta solicitud se publicará para otras ONG.
        </p>
      </div>

      {items.map((item, index) => (
        <div key={index} style={{
          border: '1px solid var(--border-light)',
          borderRadius: 'var(--border-radius-md)',
          padding: 'var(--spacing-lg)',
          marginBottom: 'var(--spacing-md)',
          background: 'var(--bg-secondary)'
        }}>
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: 'var(--spacing-md)'
          }}>
            <h4 style={{ margin: 0, color: 'var(--text-primary)' }}>Item {index + 1}</h4>
            {items.length > 1 && (
              <button
                type="button"
                className="btn btn-danger btn-sm"
                onClick={() => handleRemoveItem(index)}
                disabled={isSubmitting}
              >
                ❌ Remover
              </button>
            )}
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: 'var(--spacing-lg)',
            marginBottom: 'var(--spacing-md)'
          }}>
            <div className="form-group">
              <label className="form-label">📦 Categoría</label>
              <select
                value={item.category}
                onChange={(e) => handleItemChange(index, 'category', e.target.value)}
                required
                className="form-select"
                disabled={isSubmitting}
              >
                <option value="">Seleccionar...</option>
                {categorias.map(cat => (
                  <option key={cat.value} value={cat.value}>{cat.label}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">🔢 Cantidad Necesaria</label>
              <input
                type="number"
                value={item.quantity}
                onChange={(e) => handleItemChange(index, 'quantity', e.target.value)}
                min="1"
                className="form-input"
                disabled={isSubmitting}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">📝 Descripción</label>
            <textarea
              value={item.description}
              onChange={(e) => handleItemChange(index, 'description', e.target.value)}
              required
              className="form-textarea"
              placeholder="Describe detalladamente lo que necesitas..."
              rows="2"
              disabled={isSubmitting}
            />
          </div>
        </div>
      ))}

      <div style={{ marginBottom: 'var(--spacing-lg)' }}>
        <button
          type="button"
          className="btn btn-secondary"
          onClick={handleAddItem}
          disabled={isSubmitting}
        >
          ➕ Agregar Otro Item
        </button>
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
              📤 Publicar Solicitud
            </>
          )}
        </button>
      </div>
    </form>
  );
};

export default DonationRequestForm;
