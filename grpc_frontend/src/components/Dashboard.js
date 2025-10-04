import React, { useEffect, useState } from 'react';
import axios from 'axios';
import UserForm from './UserForm';
import EventForm from './EventForm';
import InventoryForm from './InventoryForm';

const Dashboard = ({ currentUser, onLogout }) => {
  const [usuarios, setUsuarios] = useState([]);
  const [eventos, setEventos] = useState([]);
  const [donaciones, setDonaciones] = useState([]);
  const [activeTab, setActiveTab] = useState(() => {
    if (currentUser && currentUser.rol) {
      const userRole = currentUser.rol.toUpperCase();
      if (userRole === 'PRESIDENTE') {
        return 'usuarios';
      } else if (userRole === 'COORDINADOR' || userRole === 'VOLUNTARIO') {
        return 'eventos';
      } else if (userRole === 'VOCAL') {
        return 'donaciones';
      }
    }
    return 'usuarios';
  });
  const [showForm, setShowForm] = useState(false);
  const [selectedItem, setSelectedItem] = useState(null);
  const [formType, setFormType] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showMemberModal, setShowMemberModal] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);

  useEffect(() => {
    fetchData();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  const fetchData = async () => {
    setLoading(true);
    try {
      await Promise.all([
        fetchUsuarios(),
        fetchEventos(),
        fetchDonaciones()
      ]);
    } finally {
      setLoading(false);
    }
  };

  const fetchUsuarios = async () => {
    try {
      const response = await axios.get('http://localhost:5000/usuarios');
      setUsuarios(response.data);
    } catch (error) {
      console.error('Error fetching usuarios:', error);
    }
  };

  const fetchEventos = async () => {
    try {
      const response = await axios.get('http://localhost:5000/eventos');
      setEventos(response.data);
    } catch (error) {
      console.error('Error fetching eventos:', error);
    }
  };

  const fetchDonaciones = async () => {
    try {
      const response = await axios.get('http://localhost:5000/donaciones');
      setDonaciones(response.data);
    } catch (error) {
      console.error('Error fetching donaciones:', error);
    }
  };

  // Funciones de verificación de permisos
  const hasPermission = (permission) => {
    if (!currentUser || !currentUser.rol) return false;

    const userRole = currentUser.rol.toUpperCase();

    switch (permission) {
      case 'manage_users':
        return userRole === 'PRESIDENTE';
      case 'manage_events':
        return userRole === 'PRESIDENTE' || userRole === 'COORDINADOR';
      case 'manage_inventory':
        return userRole === 'PRESIDENTE' || userRole === 'VOCAL';
      case 'view_events':
        return userRole === 'PRESIDENTE' || userRole === 'VOCAL' || userRole === 'COORDINADOR' || userRole === 'VOLUNTARIO';
      case 'join_events':
        return userRole === 'VOLUNTARIO';
      case 'view_all':
        return true; // Todos pueden ver
      default:
        return false;
    }
  };

  const handleCreate = (type) => {
    setFormType(type);
    setSelectedItem(null);
    setShowForm(true);
  };

  const handleEdit = (item, type) => {
    setFormType(type);
    setSelectedItem(item);
    setShowForm(true);
  };

  const handleDelete = async (id, type) => {
    try {
      let endpoint;
      if (type === 'user') {
        endpoint = `http://localhost:5000/usuarios/${id}`;
      } else if (type === 'event') {
        endpoint = `http://localhost:5000/eventos/${id}`;
      } else if (type === 'inventory') {
        endpoint = `http://localhost:5000/donaciones/${id}`;
      }

      await axios.delete(endpoint, { data: { userId: currentUser.id } });

      // Refresh data
      if (type === 'user') {
        await fetchUsuarios();
        await fetchEventos();
      } else if (type === 'event') {
        await fetchEventos();
      } else if (type === 'inventory') {
        await fetchDonaciones();
      }
    } catch (error) {
      console.error('Error deleting item:', error);
    }
  };

  const handleFormSuccess = () => {
    setShowForm(false);
    setSelectedItem(null);
    setFormType(null);
    fetchData(); // Refresh all data
  };

  const handleFormCancel = () => {
    setShowForm(false);
    setSelectedItem(null);
    setFormType(null);
  };

  // eslint-disable-next-line no-unused-vars
  const showAlert = (message, type) => {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.innerHTML = `<span>${message}</span>`;
    alertDiv.style.position = 'fixed';
    alertDiv.style.top = '20px';
    alertDiv.style.right = '20px';
    alertDiv.style.zIndex = '9999';
    alertDiv.style.maxWidth = '400px';

    document.body.appendChild(alertDiv);

    setTimeout(() => {
      alertDiv.remove();
    }, 5000);
  };

  const handleJoinLeaveEvent = async (event) => {
    const eventDate = new Date(event.fechaHora);
    const now = new Date();
    const isFuture = eventDate > now;
    const isMember = event.miembros && event.miembros.some(m => m.id === currentUser.id);

    if (!isFuture && !isMember) {
      return;
    }

    try {
      const endpoint = `http://localhost:5000/eventos/${event.id}/miembros/${currentUser.id}`;
      if (isMember) {
        await axios.delete(endpoint, { data: { userId: currentUser.id } });
      } else {
        await axios.post(endpoint, { userId: currentUser.id });
      }
      await fetchEventos(); // Refresh events
    } catch (error) {
      console.error('Error joining/leaving event:', error);
    }
  };

  const handleManageMembers = (event) => {
    setSelectedEvent(event);
    setShowMemberModal(true);
  };

  const handleMemberModalClose = () => {
    setShowMemberModal(false);
    setSelectedEvent(null);
  };

  const handleAddMember = async (userId) => {
    try {
      await axios.post(`http://localhost:5000/eventos/${selectedEvent.id}/miembros/${userId}`, { userId: currentUser.id });
      await fetchEventos();
    } catch (error) {
      console.error('Error adding member:', error);
    }
  };

  const handleRemoveMember = async (userId) => {
    try {
      await axios.delete(`http://localhost:5000/eventos/${selectedEvent.id}/miembros/${userId}`, { data: { userId: currentUser.id } });
      await fetchEventos();
    } catch (error) {
      console.error('Error removing member:', error);
    }
  };

  const renderStats = () => (
    <div style={{
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
      gap: 'var(--spacing-lg)',
      marginBottom: 'var(--spacing-2xl)'
    }}>
      <div style={{
        background: 'linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%)',
        color: 'white',
        padding: 'var(--spacing-lg)',
        borderRadius: 'var(--border-radius-lg)',
        textAlign: 'center',
        boxShadow: 'var(--shadow-md)'
      }}>
        <div style={{ fontSize: '2rem', fontWeight: '700', marginBottom: 'var(--spacing-xs)' }}>
          👥
        </div>
        <div style={{ fontSize: '2rem', fontWeight: '700' }}>
          {usuarios.length}
        </div>
        <div style={{ fontSize: '0.9rem', opacity: '0.9' }}>
          Usuarios
        </div>
      </div>

      <div style={{
        background: 'linear-gradient(135deg, var(--accent-color) 0%, var(--accent-hover) 100%)',
        color: 'white',
        padding: 'var(--spacing-lg)',
        borderRadius: 'var(--border-radius-lg)',
        textAlign: 'center',
        boxShadow: 'var(--shadow-md)'
      }}>
        <div style={{ fontSize: '2rem', fontWeight: '700', marginBottom: 'var(--spacing-xs)' }}>
          📅
        </div>
        <div style={{ fontSize: '2rem', fontWeight: '700' }}>
          {eventos.length}
        </div>
        <div style={{ fontSize: '0.9rem', opacity: '0.9' }}>
          Eventos
        </div>
      </div>

      <div style={{
        background: 'linear-gradient(135deg, var(--success-color) 0%, var(--success-hover) 100%)',
        color: 'white',
        padding: 'var(--spacing-lg)',
        borderRadius: 'var(--border-radius-lg)',
        textAlign: 'center',
        boxShadow: 'var(--shadow-md)'
      }}>
        <div style={{ fontSize: '2rem', fontWeight: '700', marginBottom: 'var(--spacing-xs)' }}>
          📦
        </div>
        <div style={{ fontSize: '2rem', fontWeight: '700' }}>
          {donaciones.length}
        </div>
        <div style={{ fontSize: '0.9rem', opacity: '0.9' }}>
          Donaciones
        </div>
      </div>
    </div>
  );

  if (loading) {
    return (
      <div style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      }}>
        <div style={{ textAlign: 'center', color: 'white' }}>
          <div className="spinner" style={{ width: '3rem', height: '3rem', margin: '0 auto var(--spacing-lg)' }}></div>
          <h2>Cargando Dashboard...</h2>
          <p>Preparando tu espacio de trabajo</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="card">
        <div className="card-header" style={{ position: 'relative' }}>
          <button
            className="btn btn-secondary"
            onClick={onLogout}
            style={{
              position: 'absolute',
              top: 'var(--spacing-md)',
              left: 'var(--spacing-md)',
              background: 'var(--danger-color)',
              color: 'white',
              border: 'none',
              padding: 'var(--spacing-sm) var(--spacing-md)',
              borderRadius: 'var(--border-radius-md)',
              cursor: 'pointer',
              fontSize: '0.9rem',
              display: 'flex',
              alignItems: 'center',
              gap: 'var(--spacing-xs)'
            }}
          >
            🚪 Cerrar Sesión
          </button>
          <h1 style={{
            marginBottom: 'var(--spacing-sm)',
            background: 'linear-gradient(135deg, var(--primary-color) 0%, var(--accent-color) 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            backgroundClip: 'text',
            textAlign: 'center'
          }}>
            🏢 Dashboard de Gestión Solidaria
          </h1>
          <p style={{ color: 'var(--text-secondary)', margin: 0, fontSize: '1.1rem', textAlign: 'center' }}>
            Gestiona usuarios, eventos y donaciones de manera eficiente
          </p>
        </div>

        {renderStats()}

        <div className="tabs">
          {hasPermission('manage_users') && (
            <button
              className={`tab-button ${activeTab === 'usuarios' ? 'active' : ''}`}
              onClick={() => setActiveTab('usuarios')}
            >
              👥 Usuarios ({usuarios.length})
            </button>
          )}
          {hasPermission('view_events') && (
            <button
              className={`tab-button ${activeTab === 'eventos' ? 'active' : ''}`}
              onClick={() => setActiveTab('eventos')}
            >
              📅 Eventos ({eventos.length})
            </button>
          )}
          {hasPermission('manage_inventory') && (
            <button
              className={`tab-button ${activeTab === 'donaciones' ? 'active' : ''}`}
              onClick={() => setActiveTab('donaciones')}
            >
              📦 Donaciones ({donaciones.length})
            </button>
          )}
        </div>

        <div>
          {activeTab === 'usuarios' && hasPermission('manage_users') && (
            <div>
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: 'var(--spacing-lg)',
                flexWrap: 'wrap',
                gap: 'var(--spacing-md)'
              }}>
                <h2 style={{ margin: 0, color: 'var(--text-primary)' }}>Gestión de Usuarios</h2>
                <button className="btn btn-primary" onClick={() => handleCreate('user')}>
                  ➕ Nuevo Usuario
                </button>
              </div>

              <div style={{ overflowX: 'auto', borderRadius: 'var(--border-radius-md)' }}>
                <table className="table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>👤 Usuario</th>
                      <th>📝 Nombre</th>
                      <th>📞 Contacto</th>
                      <th>🏷️ Rol</th>
                      <th>📊 Estado</th>
                      <th>⚙️ Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {usuarios.map(u => (
                      <tr key={u.id}>
                        <td style={{ fontWeight: '600', color: 'var(--primary-color)' }}>
                          #{u.id}
                        </td>
                        <td>
                          <div>
                            <div style={{ fontWeight: '500' }}>{u.nombreUsuario}</div>
                            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                              {u.email}
                            </div>
                          </div>
                        </td>
                        <td>{u.nombre} {u.apellido}</td>
                        <td>{u.telefono}</td>
                        <td>
                          <span className="category-tag">
                            {u.rol}
                          </span>
                        </td>
                        <td>
                          <span className={`status-badge ${u.activo ? 'status-active' : 'status-inactive'}`}>
                            {u.activo ? '✓ Activo' : '✗ Inactivo'}
                          </span>
                        </td>
                        <td>
                          <div className="action-buttons">
                            <button className="btn btn-secondary btn-sm" onClick={() => handleEdit(u, 'user')}>
                              ✏️ Editar
                            </button>
                            <button className="btn btn-danger btn-sm" onClick={() => handleDelete(u.id, 'user')}>
                              🗑️ Eliminar
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {activeTab === 'eventos' && hasPermission('view_events') && (
            <div>
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: 'var(--spacing-lg)',
                flexWrap: 'wrap',
                gap: 'var(--spacing-md)'
              }}>
                <h2 style={{ margin: 0, color: 'var(--text-primary)' }}>Gestión de Eventos</h2>
                {hasPermission('manage_events') && (
                  <button className="btn btn-primary" onClick={() => handleCreate('event')}>
                    ➕ Nuevo Evento
                  </button>
                )}
              </div>

              <div style={{ overflowX: 'auto', borderRadius: 'var(--border-radius-md)' }}>
                <table className="table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>📅 Evento</th>
                      <th>📝 Descripción</th>
                      <th>🕒 Fecha y Hora</th>
                      <th>👥 Miembros</th>
                      <th>⚙️ Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {eventos.map(e => (
                      <tr key={e.id}>
                        <td style={{ fontWeight: '600', color: 'var(--primary-color)' }}>
                          #{e.id}
                        </td>
                        <td style={{ fontWeight: '500' }}>{e.nombre}</td>
                        <td>
                          <div style={{
                            maxWidth: '300px',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap'
                          }}>
                            {e.descripcion}
                          </div>
                        </td>
                        <td>
                          <div>
                            <div>{new Date(e.fechaHora).toLocaleDateString()}</div>
                            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                              {new Date(e.fechaHora).toLocaleTimeString()}
                            </div>
                          </div>
                        </td>
                        <td>
                          {e.miembros && e.miembros.length > 0 ? (
                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 'var(--spacing-xs)' }}>
                              {e.miembros.slice(0, 3).map(m => (
                                <span key={m.id} className="category-tag" style={{ fontSize: '0.7rem' }}>
                                  {m.nombre}
                                </span>
                              ))}
                              {e.miembros.length > 3 && (
                                <span className="category-tag" style={{ fontSize: '0.7rem' }}>
                                  +{e.miembros.length - 3}
                                </span>
                              )}
                            </div>
                          ) : (
                            <span style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>
                              Sin miembros
                            </span>
                          )}
                        </td>
                        <td>
                          <div className="action-buttons">
                            {hasPermission('manage_events') && (
                              <>
                                <button className="btn btn-secondary btn-sm" onClick={() => handleEdit(e, 'event')}>
                                  ✏️ Editar
                                </button>
                                <button className="btn btn-danger btn-sm" onClick={() => handleDelete(e.id, 'event')}>
                                  🗑️ Eliminar
                                </button>
                              </>
                            )}
                            {hasPermission('join_events') && (
                              <button className="btn btn-primary btn-sm" onClick={() => handleJoinLeaveEvent(e)}>
                                {e.miembros && e.miembros.some(m => m.id === currentUser.id) ? '🚪 Abandonar' : '➕ Unirse'}
                              </button>
                            )}
                            {hasPermission('manage_events') && (
                              <button className="btn btn-secondary btn-sm" onClick={() => handleManageMembers(e)}>
                                👥 Gestionar Miembros
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {activeTab === 'donaciones' && hasPermission('manage_inventory') && (
            <div>
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: 'var(--spacing-lg)',
                flexWrap: 'wrap',
                gap: 'var(--spacing-md)'
              }}>
                <h2 style={{ margin: 0, color: 'var(--text-primary)' }}>Gestión de Donaciones</h2>
                <button className="btn btn-primary" onClick={() => handleCreate('inventory')}>
                  ➕ Nueva Donación
                </button>
              </div>

              <div style={{ overflowX: 'auto', borderRadius: 'var(--border-radius-md)' }}>
                <table className="table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>📦 Categoría</th>
                      <th>📝 Descripción</th>
                      <th>🔢 Cantidad</th>
                      <th>📊 Estado</th>
                      <th>⚙️ Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {donaciones.map(d => (
                      <tr key={d.id}>
                        <td style={{ fontWeight: '600', color: 'var(--primary-color)' }}>
                          #{d.id}
                        </td>
                        <td>
                          <span className="category-tag">
                            {d.categoria === 0 ? '👕 ROPA' :
                             d.categoria === 1 ? '🍕 ALIMENTOS' :
                             d.categoria === 2 ? '🧸 JUGUETES' :
                             d.categoria === 3 ? '📚 ÚTILES' : 'OTROS'}
                          </span>
                        </td>
                        <td>{d.descripcion}</td>
                        <td>
                          <span style={{
                            fontWeight: '700',
                            fontSize: '1.1rem',
                            color: 'var(--primary-color)'
                          }}>
                            {d.cantidad}
                          </span>
                        </td>
                        <td>
                          <span className={`status-badge ${d.eliminado ? 'status-inactive' : 'status-active'}`}>
                            {d.eliminado ? '❌ Eliminado' : '✅ Disponible'}
                          </span>
                        </td>
                        <td>
                          <div className="action-buttons">
                            <button className="btn btn-secondary btn-sm" onClick={() => handleEdit(d, 'inventory')}>
                              ✏️ Editar
                            </button>
                            <button className="btn btn-danger btn-sm" onClick={() => handleDelete(d.id, 'inventory')}>
                              🗑️ Eliminar
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>

      {showForm && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2 className="modal-title">
                {formType === 'user' && (selectedItem ? '✏️ Editar Usuario' : '➕ Crear Usuario')}
                {formType === 'event' && (selectedItem ? '✏️ Editar Evento' : '➕ Crear Evento')}
                {formType === 'inventory' && (selectedItem ? '✏️ Editar Donación' : '➕ Crear Donación')}
              </h2>
              <button className="close-button" onClick={handleFormCancel}>×</button>
            </div>
            <div>
              {formType === 'user' && (
                <UserForm
                  user={selectedItem}
                  onSuccess={handleFormSuccess}
                  onCancel={handleFormCancel}
                  currentUser={currentUser}
                />
              )}
              {formType === 'event' && (
                <EventForm
                  event={selectedItem}
                  onSuccess={handleFormSuccess}
                  onCancel={handleFormCancel}
                  currentUser={currentUser}
                />
              )}
              {formType === 'inventory' && (
                <InventoryForm
                  item={selectedItem}
                  onSuccess={handleFormSuccess}
                  onCancel={handleFormCancel}
                  currentUser={currentUser}
                />
              )}
            </div>
          </div>
        </div>
      )}

      {showMemberModal && selectedEvent && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '600px' }}>
            <div className="modal-header">
              <h2 className="modal-title">👥 Gestionar Miembros - {selectedEvent.nombre}</h2>
              <button className="close-button" onClick={handleMemberModalClose}>×</button>
            </div>
            <div style={{ padding: 'var(--spacing-lg)' }}>
              <h3 style={{ marginBottom: 'var(--spacing-md)', color: 'var(--text-primary)' }}>Miembros Actuales</h3>
              {selectedEvent.miembros && selectedEvent.miembros.length > 0 ? (
                <div style={{ marginBottom: 'var(--spacing-lg)' }}>
                  {selectedEvent.miembros.map(member => (
                    <div key={member.id} style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      padding: 'var(--spacing-sm)',
                      border: '1px solid var(--border-color)',
                      borderRadius: 'var(--border-radius-sm)',
                      marginBottom: 'var(--spacing-xs)',
                      background: 'var(--background-light)'
                    }}>
                      <span>{member.nombre} {member.apellido}</span>
                      <button
                        className="btn btn-danger btn-sm"
                        onClick={() => handleRemoveMember(member.id)}
                      >
                        ❌ Remover
                      </button>
                    </div>
                  ))}
                </div>
              ) : (
                <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No hay miembros en este evento</p>
              )}

              <h3 style={{ marginBottom: 'var(--spacing-md)', color: 'var(--text-primary)' }}>Agregar Miembro</h3>
              <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                {usuarios.filter(u => u.activo && (!selectedEvent.miembros || !selectedEvent.miembros.some(m => m.id === u.id))).map(user => (
                  <div key={user.id} style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: 'var(--spacing-sm)',
                    border: '1px solid var(--border-color)',
                    borderRadius: 'var(--border-radius-sm)',
                    marginBottom: 'var(--spacing-xs)',
                    background: 'var(--background-light)'
                  }}>
                    <span>{user.nombre} {user.apellido} ({user.rol})</span>
                    <button
                      className="btn btn-primary btn-sm"
                      onClick={() => handleAddMember(user.id)}
                    >
                      ➕ Agregar
                    </button>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
