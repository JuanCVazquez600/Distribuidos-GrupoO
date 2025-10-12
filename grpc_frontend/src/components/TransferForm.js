import React, { useEffect, useState } from 'react';
import axios from 'axios';

const TransferForm = ({ currentUser, onSuccess, onCancel }) => {
  const [requests, setRequests] = useState([]);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [selectedItems, setSelectedItems] = useState([]);
  const [recipientOrg, setRecipientOrg] = useState('');
  const [organizations, setOrganizations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [transferring, setTransferring] = useState(false);

  // Organization ID from backend application.properties (spring.organization.id=org-300)
  const ORGANIZATION_ID = 'org-300';

  useEffect(() => {
    fetchRequests();
    fetchOrganizations();
  }, []);

  const fetchRequests = async () => {
    try {
      const response = await axios.get('http://localhost:5000/requests/list');
      setRequests(response.data);
    } catch (error) {
      console.error('Error fetching requests:', error);
    }
  };

  const fetchOrganizations = async () => {
    try {
      // Get unique orgs from requests, excluding own
      const response = await axios.get('http://localhost:5000/requests/list');
      const orgs = [...new Set(response.data.map(r => r.organizationId))].filter(org => org !== ORGANIZATION_ID);
      setOrganizations(orgs);
    } catch (error) {
      console.error('Error fetching organizations:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectRequest = (request) => {
    setSelectedRequest(request);
    setSelectedItems(request.requestedDonations.map(item => ({ ...item, selected: false, quantity: 1 })));
  };

  const handleItemToggle = (index) => {
    const newItems = [...selectedItems];
    newItems[index].selected = !newItems[index].selected;
    setSelectedItems(newItems);
  };

  const handleQuantityChange = (index, quantity) => {
    const newItems = [...selectedItems];
    newItems[index].quantity = parseInt(quantity) || 1;
    setSelectedItems(newItems);
  };

  const handleTransfer = async () => {
    if (!selectedRequest || !recipientOrg || !selectedItems.some(item => item.selected)) {
      alert('Selecciona una solicitud, organización destinataria e items a transferir');
      return;
    }

    setTransferring(true);
    try {
      const transferData = {
        transferId: `transfer-${Date.now()}`,
        donations: selectedItems
          .filter(item => item.selected)
          .map(item => ({
            category: item.category,
            description: item.description,
            quantity: item.quantity.toString() + ' unidades' // Match backend expectation
          }))
      };

      await axios.post(`http://localhost:5000/transfers/send/${recipientOrg}`, transferData);
      alert('Transferencia enviada correctamente');
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
      alert('Error al enviar transferencia: ' + errorMessage);
    } finally {
      setTransferring(false);
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
        <span style={{ marginLeft: 'var(--spacing-md)' }}>Cargando solicitudes...</span>
      </div>
    );
  }

  return (
    <div style={{ width: '100%' }}>
      <div style={{ marginBottom: 'var(--spacing-lg)' }}>
        <h3 style={{ marginBottom: 'var(--spacing-md)', color: 'var(--text-primary)' }}>
          🚚 Transferir Donaciones
        </h3>
        <p style={{ color: 'var(--text-secondary)', marginBottom: 'var(--spacing-lg)' }}>
          Selecciona una solicitud de otra organización y transfiere items desde tu inventario.
        </p>
      </div>

      {!selectedRequest ? (
        <div>
          <h4 style={{ marginBottom: 'var(--spacing-md)', color: 'var(--text-primary)' }}>
            📋 Solicitudes Disponibles
          </h4>
          {requests.filter(r => r.organizationId !== ORGANIZATION_ID).length === 0 ? (
            <div style={{
              textAlign: 'center',
              padding: 'var(--spacing-xl)',
              color: 'var(--text-muted)'
            }}>
              No hay solicitudes de otras organizaciones
            </div>
          ) : (
            <div style={{ display: 'grid', gap: 'var(--spacing-md)' }}>
              {requests.filter(r => r.organizationId !== ORGANIZATION_ID).map(request => (
                <div key={request.requestId} style={{
                  border: '1px solid var(--border-light)',
                  borderRadius: 'var(--border-radius-md)',
                  padding: 'var(--spacing-lg)',
                  background: 'var(--bg-secondary)',
                  cursor: 'pointer'
                }} onClick={() => handleSelectRequest(request)}>
                  <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: 'var(--spacing-md)'
                  }}>
                    <div>
                      <span className="category-tag">{request.organizationId}</span>
                      <span style={{ marginLeft: 'var(--spacing-md)', fontWeight: '500' }}>
                        Solicitud #{request.requestId}
                      </span>
                    </div>
                    <span style={{ color: 'var(--primary-color)', fontWeight: '600' }}>
                      {request.requestedDonations.length} items
                    </span>
                  </div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 'var(--spacing-xs)' }}>
                    {request.requestedDonations.slice(0, 3).map((item, idx) => (
                      <span key={idx} className="category-tag" style={{ fontSize: '0.8rem' }}>
                        {item.category}: {item.description}
                      </span>
                    ))}
                    {request.requestedDonations.length > 3 && (
                      <span className="category-tag" style={{ fontSize: '0.8rem' }}>
                        +{request.requestedDonations.length - 3} más
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      ) : (
        <div>
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: 'var(--spacing-lg)'
          }}>
            <h4 style={{ margin: 0, color: 'var(--text-primary)' }}>
              📋 Solicitud Seleccionada: #{selectedRequest.requestId}
            </h4>
            <button
              className="btn btn-secondary btn-sm"
              onClick={() => setSelectedRequest(null)}
            >
              ← Cambiar Solicitud
            </button>
          </div>

          <div style={{
            border: '1px solid var(--border-light)',
            borderRadius: 'var(--border-radius-md)',
            padding: 'var(--spacing-lg)',
            marginBottom: 'var(--spacing-lg)',
            background: 'var(--bg-secondary)'
          }}>
            <div style={{ marginBottom: 'var(--spacing-md)' }}>
              <span className="category-tag">{selectedRequest.organizationId}</span>
            </div>
            <h5 style={{ marginBottom: 'var(--spacing-md)' }}>Items Solicitados:</h5>
            {selectedItems.map((item, index) => (
              <div key={index} style={{
                display: 'flex',
                alignItems: 'center',
                gap: 'var(--spacing-md)',
                marginBottom: 'var(--spacing-sm)',
                padding: 'var(--spacing-sm)',
                border: '1px solid var(--border-light)',
                borderRadius: 'var(--border-radius-sm)',
                background: 'white'
              }}>
                <input
                  type="checkbox"
                  checked={item.selected}
                  onChange={() => handleItemToggle(index)}
                />
                <span className="category-tag" style={{ fontSize: '0.8rem' }}>
                  {item.category}
                </span>
                <span style={{ flex: 1 }}>{item.description}</span>
                {item.selected && (
                  <input
                    type="number"
                    value={item.quantity}
                    onChange={(e) => handleQuantityChange(index, e.target.value)}
                    min="1"
                    style={{ width: '80px' }}
                    className="form-input"
                  />
                )}
              </div>
            ))}
          </div>

          <div className="form-group" style={{ marginBottom: 'var(--spacing-lg)' }}>
            <label className="form-label">🏢 Organización Destinataria</label>
            <select
              value={recipientOrg}
              onChange={(e) => setRecipientOrg(e.target.value)}
              required
              className="form-select"
            >
              <option value="">Seleccionar organización...</option>
              {organizations.map(org => (
                <option key={org} value={org}>{org}</option>
              ))}
            </select>
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
              disabled={transferring}
            >
              ❌ Cancelar
            </button>
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleTransfer}
              disabled={transferring || !recipientOrg || !selectedItems.some(item => item.selected)}
            >
              {transferring ? (
                <>
                  <div className="spinner"></div>
                  Transfiriendo...
                </>
              ) : (
                <>
                  🚚 Transferir
                </>
              )}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default TransferForm;
