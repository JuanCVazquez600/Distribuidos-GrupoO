import React, { useState, useEffect } from 'react';
import axios from 'axios';

const SavedFiltersManager = ({ 
  currentUser, 
  filters, 
  onFilterSelect, 
  tipoFiltro,
  onFilterSaved
}) => {
  const [savedFilters, setSavedFilters] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showSaveDialog, setShowSaveDialog] = useState(false);
  const [filterName, setFilterName] = useState('');

  // Cargar filtros guardados
  useEffect(() => {
    if (currentUser?.id) {
      loadSavedFilters();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentUser]);

  const loadSavedFilters = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await axios.get(`/api/filtros-guardados`, {
        params: {
          usuarioId: currentUser.id,
          tipoFiltro: tipoFiltro
        }
      });
      setSavedFilters(response.data);
    } catch (err) {
      setError(err.response?.data || 'Error al cargar los filtros guardados');
      console.error('Error loading saved filters:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSaveFilter = async () => {
    if (!filterName.trim()) {
      alert('Por favor, ingrese un nombre para el filtro');
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      await axios.post(`/api/filtros-guardados`, null, {
        params: {
          usuarioId: currentUser.id,
          nombre: filterName.trim(),
          tipoFiltro: tipoFiltro
        },
        data: filters
      });
      
      await loadSavedFilters();
      setShowSaveDialog(false);
      setFilterName('');
      if (onFilterSaved) {
        onFilterSaved();
      }
    } catch (err) {
      setError(err.response?.data || 'Error al guardar el filtro');
      console.error('Error saving filter:', err);
    } finally {
      setIsLoading(false);
    }
  };

  // eslint-disable-next-line no-unused-vars
  const handleDeleteFilter = async (filtroId) => {
    if (!window.confirm('¿Está seguro que desea eliminar este filtro?')) {
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      await axios.delete(`/api/filtros-guardados/${filtroId}`, {
        params: {
          usuarioId: currentUser.id
        }
      });
      await loadSavedFilters();
    } catch (err) {
      setError(err.response?.data || 'Error al eliminar el filtro');
      console.error('Error deleting filter:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleFilterSelect = async (filtroId) => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await axios.get(`/api/filtros-guardados/${filtroId}`, {
        params: {
          usuarioId: currentUser.id
        }
      });
      const filtroData = JSON.parse(response.data.filtrosJson);
      onFilterSelect(filtroData);
    } catch (err) {
      setError(err.response?.data || 'Error al cargar el filtro');
      console.error('Error loading filter:', err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ marginBottom: '20px' }}>
      <div style={{ display: 'flex', gap: '10px', marginBottom: '10px' }}>
        <select
          onChange={(e) => e.target.value && handleFilterSelect(e.target.value)}
          value=""
          style={{
            padding: '8px',
            borderRadius: '4px',
            border: '1px solid #ccc',
            flex: 1
          }}
        >
          <option value="">-- Seleccionar filtro guardado --</option>
          {savedFilters.map(filter => (
            <option key={filter.id} value={filter.id}>
              {filter.nombre}
            </option>
          ))}
        </select>
        
        <button
          onClick={() => setShowSaveDialog(true)}
          style={{
            backgroundColor: '#17a2b8',
            color: 'white',
            border: 'none',
            padding: '8px 16px',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          💾 Guardar Filtros
        </button>
      </div>

      {error && (
        <div style={{
          backgroundColor: '#f8d7da',
          color: '#721c24',
          padding: '10px',
          borderRadius: '4px',
          marginBottom: '10px'
        }}>
          {error}
        </div>
      )}

      {/* Modal de guardado */}
      {showSaveDialog && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '20px',
            borderRadius: '8px',
            width: '90%',
            maxWidth: '400px'
          }}>
            <h3 style={{ marginTop: 0 }}>Guardar Filtros</h3>
            
            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px' }}>
                Nombre del filtro:
              </label>
              <input
                type="text"
                value={filterName}
                onChange={(e) => setFilterName(e.target.value)}
                style={{
                  width: '100%',
                  padding: '8px',
                  borderRadius: '4px',
                  border: '1px solid #ccc'
                }}
                placeholder="Ingrese un nombre descriptivo..."
              />
            </div>

            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
              <button
                onClick={() => setShowSaveDialog(false)}
                style={{
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  padding: '8px 16px',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Cancelar
              </button>
              <button
                onClick={handleSaveFilter}
                disabled={isLoading}
                style={{
                  backgroundColor: '#28a745',
                  color: 'white',
                  border: 'none',
                  padding: '8px 16px',
                  borderRadius: '4px',
                  cursor: isLoading ? 'not-allowed' : 'pointer',
                  opacity: isLoading ? 0.7 : 1
                }}
              >
                {isLoading ? '⏳ Guardando...' : '💾 Guardar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SavedFiltersManager;