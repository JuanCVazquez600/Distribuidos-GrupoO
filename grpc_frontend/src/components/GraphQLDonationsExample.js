import React, { useState } from 'react';
import { useQuery, useMutation, gql } from '@apollo/client';

// Definir la consulta GraphQL
const GET_DONATIONS_STATS = gql`
  query GetDonationsStats($userId: Int!) {
    donationsStats(userId: $userId) {
      total
      activas
      eliminadas
      totalCantidad
    }
  }
`;

const GET_DONATIONS_REPORT = gql`
  query GetDonationsReport($userId: Int!, $filters: DonationFilters) {
    donationsReport(userId: $userId, filters: $filters) {
      stats {
        total
        activas
        eliminadas
        totalCantidad
      }
      donations {
        id
        categoria
        descripcion
        cantidad
        fechaAlta
        eliminado
        usuarioAlta
      }
    }
  }
`;

const GET_DONATIONS_GROUPED_REPORT = gql`
  query GetDonationsGroupedReport($userId: Int!, $filters: DonationFilters) {
    donationsGroupedReport(userId: $userId, filters: $filters) {
      stats {
        total
        activas
        eliminadas
        totalCantidad
      }
      groupedDonations {
        categoria
        eliminado
        totalCantidad
        conteoRegistros
      }
    }
  }
`;

const GET_AVAILABLE_CATEGORIES = gql`
  query GetAvailableCategories($userId: Int!) {
    availableCategories(userId: $userId)
  }
`;

const GET_SAVED_FILTERS = gql`
  query GetSavedFilters($userId: Int!) {
    savedFilters(userId: $userId) {
      id
      nombre
      filtros {
        categoria
        fechaDesde
        fechaHasta
        eliminado
      }
      fechaCreacion
    }
  }
`;

const SAVE_FILTER = gql`
  mutation SaveFilter($userId: Int!, $input: SavedFilterInput!) {
    saveFilter(userId: $userId, input: $input) {
      id
      nombre
      filtros {
        categoria
        fechaDesde
        fechaHasta
        eliminado
      }
      fechaCreacion
    }
  }
`;

const DELETE_SAVED_FILTER = gql`
  mutation DeleteSavedFilter($userId: Int!, $filterId: Int!) {
    deleteSavedFilter(userId: $userId, filterId: $filterId)
  }
`;

const UPDATE_SAVED_FILTER_NAME = gql`
  mutation UpdateSavedFilterName($userId: Int!, $filterId: Int!, $newName: String!) {
    updateSavedFilterName(userId: $userId, filterId: $filterId, newName: $newName) {
      id
      nombre
      filtros {
        categoria
        fechaDesde
        fechaHasta
        eliminado
      }
      fechaCreacion
    }
  }
`;

const GraphQLDonationsExample = ({ currentUser }) => {
  // Estado para los filtros
  const [filters, setFilters] = useState({
    categoria: '',
    fechaDesde: '',
    fechaHasta: '',
    eliminado: null // null = ambos, true = eliminados, false = activos
  });

  // Estado para filtros guardados
  const [showSaveModal, setShowSaveModal] = useState(false);
  const [filterName, setFilterName] = useState('');
  const [showSavedFilters, setShowSavedFilters] = useState(false);

  // Estado para editar filtros
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingFilter, setEditingFilter] = useState(null);
  const [editFilterName, setEditFilterName] = useState('');

  // Consulta para estadísticas básicas
  const { 
    loading: statsLoading, 
    error: statsError, 
    data: statsData 
  } = useQuery(GET_DONATIONS_STATS, {
    variables: { userId: currentUser?.id },
    skip: !currentUser?.id, // Solo ejecutar si tenemos userId
  });

  // Consulta para reporte completo con filtros
  const { 
    loading: reportLoading, 
    error: reportError, 
    data: reportData,
    refetch: refetchReport
  } = useQuery(GET_DONATIONS_GROUPED_REPORT, {
    variables: { 
      userId: currentUser?.id,
      filters: {
        ...(filters.categoria && { categoria: filters.categoria }),
        ...(filters.fechaDesde && { fechaDesde: filters.fechaDesde }),
        ...(filters.fechaHasta && { fechaHasta: filters.fechaHasta }),
        ...(filters.eliminado !== null && { eliminado: filters.eliminado })
      }
    },
    skip: !currentUser?.id,
  });

  // Consulta para obtener categorías disponibles
  const { 
    loading: categoriesLoading, 
    error: categoriesError, 
    data: categoriesData 
  } = useQuery(GET_AVAILABLE_CATEGORIES, {
    variables: { userId: currentUser?.id },
    skip: !currentUser?.id,
  });

  // Consulta para obtener filtros guardados
  const { 
    loading: savedFiltersLoading, 
    error: savedFiltersError, 
    data: savedFiltersData,
    refetch: refetchSavedFilters
  } = useQuery(GET_SAVED_FILTERS, {
    variables: { userId: currentUser?.id },
    skip: !currentUser?.id,
  });

  // Mutations para filtros guardados
  const [saveFilterMutation] = useMutation(SAVE_FILTER, {
    onCompleted: () => {
      setShowSaveModal(false);
      setFilterName('');
      refetchSavedFilters();
    },
    onError: (error) => {
      alert(`Error al guardar filtro: ${error.message}`);
    }
  });

  const [deleteFilterMutation] = useMutation(DELETE_SAVED_FILTER, {
    onCompleted: () => {
      refetchSavedFilters();
    },
    onError: (error) => {
      alert(`Error al eliminar filtro: ${error.message}`);
    }
  });

  const [updateFilterNameMutation] = useMutation(UPDATE_SAVED_FILTER_NAME, {
    onCompleted: () => {
      setShowEditModal(false);
      setEditingFilter(null);
      setEditFilterName('');
      refetchSavedFilters();
    },
    onError: (error) => {
      alert(`Error al actualizar nombre: ${error.message}`);
    }
  });

  // Funciones para manejar filtros guardados
  const handleSaveFilter = () => {
    if (!filterName.trim()) {
      alert('Por favor ingresa un nombre para el filtro');
      return;
    }

    saveFilterMutation({
      variables: {
        userId: currentUser?.id,
        input: {
          nombre: filterName.trim(),
          filtros: {
            ...(filters.categoria && { categoria: filters.categoria }),
            ...(filters.fechaDesde && { fechaDesde: filters.fechaDesde }),
            ...(filters.fechaHasta && { fechaHasta: filters.fechaHasta }),
            ...(filters.eliminado !== null && { eliminado: filters.eliminado })
          }
        }
      }
    });
  };

  const handleLoadFilter = (savedFilter) => {
    setFilters({
      categoria: savedFilter.filtros.categoria || '',
      fechaDesde: savedFilter.filtros.fechaDesde || '',
      fechaHasta: savedFilter.filtros.fechaHasta || '',
      eliminado: savedFilter.filtros.eliminado
    });
    setShowSavedFilters(false);
    refetchReport();
  };

  const handleDeleteFilter = (filterId) => {
    if (window.confirm('¿Estás seguro de que quieres eliminar este filtro?')) {
      deleteFilterMutation({
        variables: {
          userId: currentUser?.id,
          filterId
        }
      });
    }
  };

  const handleEditFilter = (filter) => {
    setEditingFilter(filter);
    setEditFilterName(filter.nombre);
    setShowEditModal(true);
  };

  const handleUpdateFilterName = () => {
    if (!editFilterName.trim()) {
      alert('Por favor ingresa un nombre para el filtro');
      return;
    }

    updateFilterNameMutation({
      variables: {
        userId: currentUser?.id,
        filterId: editingFilter.id,
        newName: editFilterName.trim()
      }
    });
  };

  // Verificar permisos del usuario
  const hasPermissions = currentUser?.rol === 'PRESIDENTE' || currentUser?.rol === 'VOCAL';

  if (!hasPermissions) {
    return (
      <div style={{ padding: '20px', textAlign: 'center' }}>
        <h3>📊 Reportes GraphQL</h3>
        <p>⚠️ Solo usuarios con rol PRESIDENTE o VOCAL pueden acceder a los reportes.</p>
        <p>Tu rol actual: <strong>{currentUser?.rol}</strong></p>
      </div>
    );
  }

  if (statsLoading || reportLoading) {
    return (
      <div style={{ padding: '20px', textAlign: 'center' }}>
        <h3>📊 Cargando Reportes GraphQL...</h3>
        <div>⏳ Obteniendo datos...</div>
      </div>
    );
  }

  if (statsError || reportError) {
    return (
      <div style={{ padding: '20px' }}>
        <h3>📊 Error en Reportes GraphQL</h3>
        {statsError && <p>❌ Error en estadísticas: {statsError.message}</p>}
        {reportError && <p>❌ Error en reporte: {reportError.message}</p>}
        <button onClick={() => {
          if (refetchReport) refetchReport();
        }}>
          🔄 Reintentar
        </button>
      </div>
    );
  }

  return (
    <div style={{ padding: '20px' }}>
      <h3>📊 Reportes GraphQL de Donaciones</h3>
      
      {/* Formulario de filtros */}
      <div style={{
        backgroundColor: '#f8f9fa',
        padding: '20px',
        borderRadius: '8px',
        marginBottom: '20px',
        border: '1px solid #dee2e6'
      }}>
        <h4 style={{ marginTop: 0, marginBottom: '15px' }}>🔍 Filtros</h4>
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '15px',
          alignItems: 'end'
        }}>
          {/* Filtro por categoría */}
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              Categoría:
            </label>
            <select
              value={filters.categoria}
              onChange={(e) => setFilters(prev => ({ ...prev, categoria: e.target.value }))}
              style={{
                width: '100%',
                padding: '8px',
                borderRadius: '4px',
                border: '1px solid #ccc'
              }}
            >
              <option value="">Todas las categorías</option>
              {categoriesLoading && <option disabled>Cargando categorías...</option>}
              {categoriesError && <option disabled>Error al cargar categorías</option>}
              {categoriesData?.availableCategories?.map(categoria => (
                <option key={categoria} value={categoria}>
                  {categoria.charAt(0).toUpperCase() + categoria.slice(1).toLowerCase()}
                </option>
              ))}
            </select>
          </div>

          {/* Filtro fecha desde */}
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              Fecha desde:
            </label>
            <input
              type="date"
              value={filters.fechaDesde}
              onChange={(e) => setFilters(prev => ({ ...prev, fechaDesde: e.target.value }))}
              style={{
                width: '100%',
                padding: '8px',
                borderRadius: '4px',
                border: '1px solid #ccc'
              }}
            />
          </div>

          {/* Filtro fecha hasta */}
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              Fecha hasta:
            </label>
            <input
              type="date"
              value={filters.fechaHasta}
              onChange={(e) => setFilters(prev => ({ ...prev, fechaHasta: e.target.value }))}
              style={{
                width: '100%',
                padding: '8px',
                borderRadius: '4px',
                border: '1px solid #ccc'
              }}
            />
          </div>

          {/* Filtro por estado eliminado */}
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              Estado:
            </label>
            <select
              value={filters.eliminado === null ? 'ambos' : (filters.eliminado ? 'eliminados' : 'activos')}
              onChange={(e) => {
                const value = e.target.value === 'ambos' ? null : e.target.value === 'eliminados';
                setFilters(prev => ({ ...prev, eliminado: value }));
              }}
              style={{
                width: '100%',
                padding: '8px',
                borderRadius: '4px',
                border: '1px solid #ccc'
              }}
            >
              <option value="ambos">Ambos</option>
              <option value="activos">Solo activos</option>
              <option value="eliminados">Solo eliminados</option>
            </select>
          </div>

          {/* Botones de acción */}
          <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
            <button
              onClick={() => refetchReport()}
              style={{
                backgroundColor: '#28a745',
                color: 'white',
                border: 'none',
                padding: '8px 16px',
                borderRadius: '4px',
                cursor: 'pointer',
                fontWeight: 'bold'
              }}
            >
              🔄 Aplicar Filtros
            </button>
            <button
              onClick={() => {
                setFilters({
                  categoria: '',
                  fechaDesde: '',
                  fechaHasta: '',
                  eliminado: null
                });
              }}
              style={{
                backgroundColor: '#6c757d',
                color: 'white',
                border: 'none',
                padding: '8px 16px',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              🗑️ Limpiar
            </button>
            <button
              onClick={() => setShowSaveModal(true)}
              style={{
                backgroundColor: '#007bff',
                color: 'white',
                border: 'none',
                padding: '8px 16px',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              💾 Guardar Filtros
            </button>
            <button
              onClick={() => setShowSavedFilters(!showSavedFilters)}
              style={{
                backgroundColor: '#17a2b8',
                color: 'white',
                border: 'none',
                padding: '8px 16px',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              📂 {showSavedFilters ? 'Ocultar' : 'Mostrar'} Filtros Guardados
            </button>
          </div>
        </div>
      </div>

      {/* Modal para guardar filtro */}
      {showSaveModal && (
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
            padding: '30px',
            borderRadius: '8px',
            minWidth: '400px',
            maxWidth: '500px'
          }}>
            <h4>💾 Guardar Filtros Actuales</h4>
            <p>Dale un nombre a este conjunto de filtros:</p>
            <input
              type="text"
              value={filterName}
              onChange={(e) => setFilterName(e.target.value)}
              placeholder="Ej: Ropa de este mes, Solo eliminados..."
              style={{
                width: '100%',
                padding: '10px',
                borderRadius: '4px',
                border: '1px solid #ccc',
                marginBottom: '20px'
              }}
              onKeyPress={(e) => {
                if (e.key === 'Enter') {
                  handleSaveFilter();
                }
              }}
            />
            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
              <button
                onClick={() => {
                  setShowSaveModal(false);
                  setFilterName('');
                }}
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
                style={{
                  backgroundColor: '#007bff',
                  color: 'white',
                  border: 'none',
                  padding: '8px 16px',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                💾 Guardar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal para editar nombre de filtro */}
      {showEditModal && editingFilter && (
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
            padding: '30px',
            borderRadius: '8px',
            minWidth: '400px',
            maxWidth: '500px'
          }}>
            <h4>✏️ Editar Nombre del Filtro</h4>
            <p>Editando: <strong>{editingFilter.nombre}</strong></p>
            <p>Nuevo nombre:</p>
            <input
              type="text"
              value={editFilterName}
              onChange={(e) => setEditFilterName(e.target.value)}
              placeholder="Nuevo nombre del filtro..."
              style={{
                width: '100%',
                padding: '10px',
                borderRadius: '4px',
                border: '1px solid #ccc',
                marginBottom: '20px'
              }}
              onKeyPress={(e) => {
                if (e.key === 'Enter') {
                  handleUpdateFilterName();
                }
              }}
            />
            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
              <button
                onClick={() => {
                  setShowEditModal(false);
                  setEditingFilter(null);
                  setEditFilterName('');
                }}
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
                onClick={handleUpdateFilterName}
                style={{
                  backgroundColor: '#ffc107',
                  color: 'black',
                  border: 'none',
                  padding: '8px 16px',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                ✏️ Actualizar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Lista de filtros guardados */}
      {showSavedFilters && (
        <div style={{
          backgroundColor: '#f8f9fa',
          padding: '20px',
          borderRadius: '8px',
          marginBottom: '20px',
          border: '1px solid #dee2e6'
        }}>
          <h4 style={{ marginTop: 0 }}>📂 Filtros Guardados</h4>
          {savedFiltersLoading && <p>Cargando filtros guardados...</p>}
          {savedFiltersError && <p style={{ color: 'red' }}>Error: {savedFiltersError.message}</p>}
          {savedFiltersData?.savedFilters?.length === 0 && (
            <p style={{ color: '#666' }}>No tienes filtros guardados aún. ¡Guarda algunos filtros para usarlos después!</p>
          )}
          {savedFiltersData?.savedFilters?.map(savedFilter => (
            <div key={savedFilter.id} style={{
              backgroundColor: 'white',
              padding: '15px',
              marginBottom: '10px',
              borderRadius: '6px',
              border: '1px solid #ddd',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center'
            }}>
              <div style={{ flex: 1 }}>
                <strong>{savedFilter.nombre}</strong>
                <div style={{ fontSize: '0.9rem', color: '#666', marginTop: '5px' }}>
                  {savedFilter.filtros.categoria && <span>📂 {savedFilter.filtros.categoria} </span>}
                  {savedFilter.filtros.fechaDesde && <span>📅 Desde: {savedFilter.filtros.fechaDesde} </span>}
                  {savedFilter.filtros.fechaHasta && <span>📅 Hasta: {savedFilter.filtros.fechaHasta} </span>}
                  {savedFilter.filtros.eliminado !== null && (
                    <span>{savedFilter.filtros.eliminado ? '🗑️ Solo eliminados' : '✅ Solo activos'}</span>
                  )}
                  {!savedFilter.filtros.categoria && !savedFilter.filtros.fechaDesde && 
                   !savedFilter.filtros.fechaHasta && savedFilter.filtros.eliminado === null && (
                    <span style={{ fontStyle: 'italic' }}>Sin filtros específicos</span>
                  )}
                </div>
                <div style={{ fontSize: '0.8rem', color: '#999', marginTop: '3px' }}>
                  Creado: {new Date(savedFilter.fechaCreacion).toLocaleDateString()}
                </div>
              </div>
              <div style={{ display: 'flex', gap: '8px' }}>
                <button
                  onClick={() => handleLoadFilter(savedFilter)}
                  style={{
                    backgroundColor: '#28a745',
                    color: 'white',
                    border: 'none',
                    padding: '6px 12px',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '0.9rem'
                  }}
                >
                  🔄 Usar
                </button>
                <button
                  onClick={() => handleEditFilter(savedFilter)}
                  style={{
                    backgroundColor: '#ffc107',
                    color: 'black',
                    border: 'none',
                    padding: '6px 12px',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '0.9rem'
                  }}
                >
                  ✏️ Editar
                </button>
                <button
                  onClick={() => handleDeleteFilter(savedFilter.id)}
                  style={{
                    backgroundColor: '#dc3545',
                    color: 'white',
                    border: 'none',
                    padding: '6px 12px',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '0.9rem'
                  }}
                >
                  🗑️ Eliminar
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
      
      {/* Estadísticas rápidas */}
      {statsData?.donationsStats && (
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', 
          gap: '15px', 
          marginBottom: '30px' 
        }}>
          <div style={{ 
            backgroundColor: '#e3f2fd', 
            padding: '15px', 
            borderRadius: '8px', 
            textAlign: 'center' 
          }}>
            <h4>📦 Total</h4>
            <div style={{ fontSize: '2rem', fontWeight: 'bold' }}>
              {statsData.donationsStats.total}
            </div>
          </div>
          <div style={{ 
            backgroundColor: '#e8f5e8', 
            padding: '15px', 
            borderRadius: '8px', 
            textAlign: 'center' 
          }}>
            <h4>✅ Activas</h4>
            <div style={{ fontSize: '2rem', fontWeight: 'bold' }}>
              {statsData.donationsStats.activas}
            </div>
          </div>
          <div style={{ 
            backgroundColor: '#fff3e0', 
            padding: '15px', 
            borderRadius: '8px', 
            textAlign: 'center' 
          }}>
            <h4>🗑️ Eliminadas</h4>
            <div style={{ fontSize: '2rem', fontWeight: 'bold' }}>
              {statsData.donationsStats.eliminadas}
            </div>
          </div>
          <div style={{ 
            backgroundColor: '#fce4ec', 
            padding: '15px', 
            borderRadius: '8px', 
            textAlign: 'center' 
          }}>
            <h4>� Total Items</h4>
            <div style={{ fontSize: '2rem', fontWeight: 'bold' }}>
              {statsData.donationsStats.totalCantidad}
            </div>
          </div>
        </div>
      )}

      {/* Reporte agrupado por categoría y estado */}
      {reportData?.donationsGroupedReport && (
        <div>
          <h4>📋 Reporte Agrupado de Donaciones</h4>
          
          {/* Indicador de filtros activos */}
          {(filters.categoria || filters.fechaDesde || filters.fechaHasta || filters.eliminado !== null) && (
            <div style={{
              backgroundColor: '#e7f3ff',
              padding: '10px',
              borderRadius: '6px',
              marginBottom: '15px',
              border: '1px solid #b3d9ff'
            }}>
              <strong>🔍 Filtros aplicados:</strong>
              {filters.categoria && <span style={{ marginLeft: '10px' }}>📂 Categoría: {filters.categoria}</span>}
              {filters.fechaDesde && <span style={{ marginLeft: '10px' }}>📅 Desde: {filters.fechaDesde}</span>}
              {filters.fechaHasta && <span style={{ marginLeft: '10px' }}>📅 Hasta: {filters.fechaHasta}</span>}
              {filters.eliminado !== null && (
                <span style={{ marginLeft: '10px' }}>
                  {filters.eliminado ? '🗑️ Solo eliminados' : '✅ Solo activos'}
                </span>
              )}
            </div>
          )}

          <div style={{ 
            backgroundColor: '#f5f5f5', 
            padding: '15px', 
            borderRadius: '8px', 
            marginBottom: '15px' 
          }}>
            <p><strong>Total registros:</strong> {reportData.donationsGroupedReport.stats.total}</p>
            <p><strong>Activos:</strong> {reportData.donationsGroupedReport.stats.activas} | <strong>Eliminados:</strong> {reportData.donationsGroupedReport.stats.eliminadas}</p>
            <p><strong>Total cantidad de items:</strong> {reportData.donationsGroupedReport.stats.totalCantidad}</p>
          </div>

          <div style={{ maxHeight: '500px', overflowY: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: 'white' }}>
              <thead>
                <tr style={{ backgroundColor: '#2196f3', color: 'white' }}>
                  <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #ddd' }}>Categoría</th>
                  <th style={{ padding: '12px', textAlign: 'center', border: '1px solid #ddd' }}>Estado</th>
                  <th style={{ padding: '12px', textAlign: 'center', border: '1px solid #ddd' }}>Registros</th>
                  <th style={{ padding: '12px', textAlign: 'center', border: '1px solid #ddd' }}>Total Cantidad</th>
                </tr>
              </thead>
              <tbody>
                {reportData.donationsGroupedReport.groupedDonations.map((group, index) => (
                  <tr key={`${group.categoria}-${group.eliminado}`} style={{ 
                    backgroundColor: group.eliminado ? '#ffebee' : 'white',
                    borderBottom: '1px solid #ddd'
                  }}>
                    <td style={{ padding: '12px', border: '1px solid #ddd', fontWeight: 'bold' }}>
                      {group.categoria}
                    </td>
                    <td style={{ padding: '12px', textAlign: 'center', border: '1px solid #ddd' }}>
                      {group.eliminado ? (
                        <span style={{ color: '#d32f2f', fontWeight: 'bold' }}>🗑️ Eliminado</span>
                      ) : (
                        <span style={{ color: '#388e3c', fontWeight: 'bold' }}>✅ Activo</span>
                      )}
                    </td>
                    <td style={{ padding: '12px', textAlign: 'center', border: '1px solid #ddd' }}>
                      {group.conteoRegistros}
                    </td>
                    <td style={{ padding: '12px', textAlign: 'center', border: '1px solid #ddd', fontWeight: 'bold' }}>
                      {group.totalCantidad}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <div style={{ marginTop: '20px', textAlign: 'center' }}>
        <button 
          onClick={() => refetchReport()}
          style={{ 
            backgroundColor: '#2196F3', 
            color: 'white', 
            border: 'none', 
            padding: '10px 20px', 
            borderRadius: '5px', 
            cursor: 'pointer',
            fontSize: '16px'
          }}
        >
          🔄 Actualizar Reportes
        </button>
      </div>
    </div>
  );
};

export default GraphQLDonationsExample;