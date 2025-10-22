import React from 'react';
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

const GraphQLDonationsExample = ({ currentUser }) => {
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
  } = useQuery(GET_DONATIONS_REPORT, {
    variables: { 
      userId: currentUser?.id,
      filters: {
        eliminado: false // Mostrar solo donaciones activas
      }
    },
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

      {/* Reporte detallado */}
      {reportData?.donationsReport && (
        <div>
          <h4>📋 Inventario de Donaciones</h4>
          <div style={{ 
            backgroundColor: '#f5f5f5', 
            padding: '15px', 
            borderRadius: '8px', 
            marginBottom: '15px' 
          }}>
            <p><strong>Total en reporte:</strong> {reportData.donationsReport.stats.total}</p>
            <p><strong>Activas:</strong> {reportData.donationsReport.stats.activas}</p>
            <p><strong>Total cantidad de items:</strong> {reportData.donationsReport.stats.totalCantidad}</p>
          </div>

          <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
            {reportData.donationsReport.donations.map((donation) => (
              <div key={donation.id} style={{ 
                backgroundColor: donation.eliminado ? '#ffebee' : 'white', 
                margin: '10px 0', 
                padding: '15px', 
                borderRadius: '8px', 
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                opacity: donation.eliminado ? 0.7 : 1
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <h5 style={{ margin: 0 }}>{donation.categoria}</h5>
                  <span style={{ 
                    backgroundColor: donation.eliminado ? '#f44336' : '#4caf50',
                    color: 'white',
                    padding: '4px 8px',
                    borderRadius: '12px',
                    fontSize: '0.8rem'
                  }}>
                    {donation.eliminado ? '❌ Eliminada' : '✅ Activa'}
                  </span>
                </div>
                <p style={{ margin: '8px 0', color: '#333' }}>{donation.descripcion}</p>
                <div style={{ 
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
                  gap: '10px',
                  fontSize: '0.9rem', 
                  color: '#666' 
                }}>
                  <span>📦 Cantidad: <strong>{donation.cantidad}</strong></span>
                  <span>📅 Fecha: <strong>{new Date(donation.fechaAlta).toLocaleDateString()}</strong></span>
                  <span>👤 Usuario: <strong>{donation.usuarioAlta}</strong></span>
                  <span>🆔 ID: <strong>{donation.id}</strong></span>
                </div>
              </div>
            ))}
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
            cursor: 'pointer' 
          }}
        >
          🔄 Actualizar Datos
        </button>
      </div>
    </div>
  );
};

export default GraphQLDonationsExample;