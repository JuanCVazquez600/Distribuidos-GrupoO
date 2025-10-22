import React, { useState } from 'react';
import { useQuery, gql } from '@apollo/client';

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

const GraphQLDonationsExample = ({ currentUser }) => {
  // Estado para los filtros
  const [filters, setFilters] = useState({
    categoria: '',
    fechaDesde: '',
    fechaHasta: '',
    eliminado: null // null = ambos, true = eliminados, false = activos
  });

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
          <div style={{ display: 'flex', gap: '10px' }}>
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
          </div>
        </div>
      </div>
      
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