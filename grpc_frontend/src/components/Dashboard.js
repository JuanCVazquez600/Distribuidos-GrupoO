import React, { useEffect, useState } from "react";
import axios from "axios";
import UserForm from "./UserForm";
import EventForm from "./EventForm";
import InventoryForm from "./InventoryForm";
import DonationRequestForm from "./DonationRequestForm";
import DonationOfferForm from "./DonationOfferForm";
import ExternalEventForm from "./ExternalEventForm";
import ExternalEventsList from "./ExternalEventsList";
import TransferForm from "./TransferForm";
import SoapQuery from "./SoapQuery";
import GraphQLDonationsExample from "./GraphQLDonationsExample";
import EventParticipationReport from "./EventParticipationReport";
import ExcelReportDownloader from "./ExcelReportDownloader";
import EventFilterManager from "./EventFilterManager";

const Dashboard = ({ currentUser, onLogout }) => {
  const [usuarios, setUsuarios] = useState([]);
  const [eventos, setEventos] = useState([]);
  const [donaciones, setDonaciones] = useState([]);
  const [solicitudes, setSolicitudes] = useState([]);
  const [ofertas, setOfertas] = useState([]);
  const [transferencias, setTransferencias] = useState([]);
  const [eventosExternos, setEventosExternos] = useState([]);
  const [activeTab, setActiveTab] = useState(() => {
    if (currentUser && currentUser.rol) {
      const userRole = currentUser.rol.toUpperCase();
      if (userRole === "PRESIDENTE") {
        return "usuarios";
      } else if (userRole === "COORDINADOR" || userRole === "VOLUNTARIO") {
        return "eventos";
      } else if (userRole === "VOCAL") {
        return "donaciones";
      }
    }
    return "usuarios";
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
        fetchDonaciones(),
        fetchSolicitudes(),
        fetchOfertas(),
        fetchTransferencias(),
        fetchEventosExternos(),
      ]);
    } finally {
      setLoading(false);
    }
  };

  const fetchUsuarios = async () => {
    try {
      const response = await axios.get("http://localhost:5000/usuarios");
      setUsuarios(response.data);
    } catch (error) {
      console.error("Error fetching usuarios:", error);
    }
  };

  const fetchEventos = async () => {
    try {
      const response = await axios.get("http://localhost:5000/eventos");
      setEventos(response.data);
    } catch (error) {
      console.error("Error fetching eventos:", error);
    }
  };

  const fetchDonaciones = async () => {
    try {
      const response = await axios.get("http://localhost:5000/donaciones");
      setDonaciones(response.data);
    } catch (error) {
      console.error("Error fetching donaciones:", error);
    }
  };

  const fetchSolicitudes = async () => {
    try {
      const response = await axios.get("http://localhost:5000/requests/list");
      setSolicitudes(response.data);
    } catch (error) {
      console.error("Error fetching solicitudes:", error);
    }
  };

  const fetchOfertas = async () => {
    try {
      const response = await axios.get("http://localhost:5000/offers/list");
      setOfertas(response.data);
    } catch (error) {
      console.error("Error fetching ofertas:", error);
    }
  };

  const fetchTransferencias = async () => {
    try {
      const response = await axios.get("http://localhost:5000/transfers/list");
      setTransferencias(response.data);
    } catch (error) {
      console.error("Error fetching transferencias:", error);
    }
  };

  const fetchEventosExternos = async () => {
    try {
      const response = await axios.get("http://localhost:5000/events/external");
      setEventosExternos(response.data);
    } catch (error) {
      console.error("Error fetching eventos externos:", error);
    }
  };

  // Funciones de verificación de permisos
  const hasPermission = (permission) => {
    if (!currentUser || !currentUser.rol) return false;

    const userRole = currentUser.rol.toUpperCase();

    switch (permission) {
      case "manage_users":
        return userRole === "PRESIDENTE";
      case "manage_events":
        return userRole === "PRESIDENTE" || userRole === "COORDINADOR";
      case "manage_inventory":
        return userRole === "PRESIDENTE" || userRole === "VOCAL";
      case "view_events":
        return (
          userRole === "PRESIDENTE" ||
          userRole === "VOCAL" ||
          userRole === "COORDINADOR" ||
          userRole === "VOLUNTARIO"
        );
      case "join_events":
        return userRole === "VOLUNTARIO";
      case "manage_requests":
        return userRole === "PRESIDENTE" || userRole === "VOCAL";
      case "manage_offers":
        return userRole === "PRESIDENTE" || userRole === "VOCAL";
      case "manage_transfers":
        return userRole === "PRESIDENTE" || userRole === "COORDINADOR";
      case "manage_external_events":
        return (
          userRole === "PRESIDENTE" ||
          userRole === "COORDINADOR" ||
          userRole === "VOLUNTARIO"
        );
      case "view_all":
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
      if (type === "user") {
        endpoint = `http://localhost:5000/usuarios/${id}`;
      } else if (type === "event") {
        endpoint = `http://localhost:5000/eventos/${id}`;
      } else if (type === "inventory") {
        endpoint = `http://localhost:5000/donaciones/${id}`;
      }

      await axios.delete(endpoint, { data: { userId: currentUser.id } });

      // Refresh data
      if (type === "user") {
        await fetchUsuarios();
        await fetchEventos();
      } else if (type === "event") {
        await fetchEventos();
      } else if (type === "inventory") {
        await fetchDonaciones();
      }
    } catch (error) {
      console.error("Error deleting item:", error);
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
    const alertDiv = document.createElement("div");
    alertDiv.className = `alert alert-${type}`;
    alertDiv.innerHTML = `<span>${message}</span>`;
    alertDiv.style.position = "fixed";
    alertDiv.style.top = "20px";
    alertDiv.style.right = "20px";
    alertDiv.style.zIndex = "9999";
    alertDiv.style.maxWidth = "400px";

    document.body.appendChild(alertDiv);

    setTimeout(() => {
      alertDiv.remove();
    }, 5000);
  };

  const handleJoinLeaveEvent = async (event) => {
    const eventDate = new Date(event.fechaHora);
    const now = new Date();
    const isFuture = eventDate > now;
    const isMember =
      event.miembros && event.miembros.some((m) => m.id === currentUser.id);

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
      console.error("Error joining/leaving event:", error);
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
      await axios.post(
        `http://localhost:5000/eventos/${selectedEvent.id}/miembros/${userId}`,
        { userId: currentUser.id }
      );
      await fetchEventos();
    } catch (error) {
      console.error("Error adding member:", error);
    }
  };

  const handleRemoveMember = async (userId) => {
    try {
      await axios.delete(
        `http://localhost:5000/eventos/${selectedEvent.id}/miembros/${userId}`,
        { data: { userId: currentUser.id } }
      );
      await fetchEventos();
    } catch (error) {
      console.error("Error removing member:", error);
    }
  };

  const renderStats = () => (
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))",
        gap: "var(--spacing-lg)",
        marginBottom: "var(--spacing-2xl)",
      }}
    >
      <div
        style={{
          background:
            "linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%)",
          color: "white",
          padding: "var(--spacing-lg)",
          borderRadius: "var(--border-radius-lg)",
          textAlign: "center",
          boxShadow: "var(--shadow-md)",
        }}
      >
        <div
          style={{
            fontSize: "2rem",
            fontWeight: "700",
            marginBottom: "var(--spacing-xs)",
          }}
        >
          👥
        </div>
        <div style={{ fontSize: "2rem", fontWeight: "700" }}>
          {usuarios.length}
        </div>
        <div style={{ fontSize: "0.9rem", opacity: "0.9" }}>Usuarios</div>
      </div>

      <div
        style={{
          background:
            "linear-gradient(135deg, var(--accent-color) 0%, var(--accent-hover) 100%)",
          color: "white",
          padding: "var(--spacing-lg)",
          borderRadius: "var(--border-radius-lg)",
          textAlign: "center",
          boxShadow: "var(--shadow-md)",
        }}
      >
        <div
          style={{
            fontSize: "2rem",
            fontWeight: "700",
            marginBottom: "var(--spacing-xs)",
          }}
        >
          📅
        </div>
        <div style={{ fontSize: "2rem", fontWeight: "700" }}>
          {eventos.length}
        </div>
        <div style={{ fontSize: "0.9rem", opacity: "0.9" }}>Eventos</div>
      </div>

      <div
        style={{
          background:
            "linear-gradient(135deg, var(--success-color) 0%, var(--success-hover) 100%)",
          color: "white",
          padding: "var(--spacing-lg)",
          borderRadius: "var(--border-radius-lg)",
          textAlign: "center",
          boxShadow: "var(--shadow-md)",
        }}
      >
        <div
          style={{
            fontSize: "2rem",
            fontWeight: "700",
            marginBottom: "var(--spacing-xs)",
          }}
        >
          📦
        </div>
        <div style={{ fontSize: "2rem", fontWeight: "700" }}>
          {donaciones.length}
        </div>
        <div style={{ fontSize: "0.9rem", opacity: "0.9" }}>Donaciones</div>
      </div>
    </div>
  );

  if (loading) {
    return (
      <div
        style={{
          minHeight: "100vh",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
        }}
      >
        <div style={{ textAlign: "center", color: "white" }}>
          <div
            className="spinner"
            style={{
              width: "3rem",
              height: "3rem",
              margin: "0 auto var(--spacing-lg)",
            }}
          ></div>
          <h2>Cargando Dashboard...</h2>
          <p>Preparando tu espacio de trabajo</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="card">
        <div className="card-header" style={{ position: "relative" }}>
          <button
            className="btn btn-secondary"
            onClick={onLogout}
            style={{
              position: "absolute",
              top: "var(--spacing-md)",
              left: "var(--spacing-md)",
              background: "var(--danger-color)",
              color: "white",
              border: "none",
              padding: "var(--spacing-sm) var(--spacing-md)",
              borderRadius: "var(--border-radius-md)",
              cursor: "pointer",
              fontSize: "0.9rem",
              display: "flex",
              alignItems: "center",
              gap: "var(--spacing-xs)",
            }}
          >
            🚪 Cerrar Sesión
          </button>
          <h1
            style={{
              marginBottom: "var(--spacing-sm)",
              background:
                "linear-gradient(135deg, var(--primary-color) 0%, var(--accent-color) 100%)",
              WebkitBackgroundClip: "text",
              WebkitTextFillColor: "transparent",
              backgroundClip: "text",
              textAlign: "center",
            }}
          >
            🏢 Dashboard de Gestión Solidaria
          </h1>
          <p
            style={{
              color: "var(--text-secondary)",
              margin: 0,
              fontSize: "1.1rem",
              textAlign: "center",
            }}
          >
            Gestiona usuarios, eventos y donaciones de manera eficiente
          </p>
        </div>

        {renderStats()}

        <div className="tabs">
          {hasPermission("manage_users") && (
            <button
              className={`tab-button ${activeTab === "usuarios" ? "active" : ""
                }`}
              onClick={() => setActiveTab("usuarios")}
            >
              👥 Usuarios ({usuarios.length})
            </button>
          )}
          {hasPermission("view_events") && (
            <button
              className={`tab-button ${activeTab === "eventos" ? "active" : ""
                }`}
              onClick={() => setActiveTab("eventos")}
            >
              📅 Eventos ({eventos.length})
            </button>
          )}
          {hasPermission("manage_inventory") && (
            <button
              className={`tab-button ${activeTab === "donaciones" ? "active" : ""
                }`}
              onClick={() => setActiveTab("donaciones")}
            >
              📦 Donaciones ({donaciones.length})
            </button>
          )}
          {hasPermission("manage_requests") && (
            <button
              className={`tab-button ${activeTab === "solicitudes" ? "active" : ""
                }`}
              onClick={() => setActiveTab("solicitudes")}
            >
              📋 Solicitudes ({solicitudes.length})
            </button>
          )}
          {hasPermission("manage_offers") && (
            <button
              className={`tab-button ${activeTab === "ofertas" ? "active" : ""
                }`}
              onClick={() => setActiveTab("ofertas")}
            >
              🎁 Ofertas ({ofertas.length})
            </button>
          )}
          {hasPermission("manage_transfers") && (
            <button
              className={`tab-button ${activeTab === "transferencias" ? "active" : ""
                }`}
              onClick={() => setActiveTab("transferencias")}
            >
              🔄 Transferencias ({transferencias.length})
            </button>
          )}
          {hasPermission("manage_external_events") && (
            <button
              className={`tab-button ${activeTab === "eventos-externos" ? "active" : ""
                }`}
              onClick={() => setActiveTab("eventos-externos")}
            >
              🌍 Eventos Externos ({eventosExternos.length})
            </button>
          )}
          {currentUser &&
            currentUser.rol &&
            currentUser.rol.toUpperCase() === "PRESIDENTE" && (
              <button
                className={`tab-button ${activeTab === "soap-query" ? "active" : ""
                  }`}
                onClick={() => setActiveTab("soap-query")}
              >
                🧼 SOAP Query
              </button>
            )}

          {/* Pestaña Participación en Eventos - Para todos los roles */}
          {hasPermission("view_all") && (
            <button
              className={`tab-button ${activeTab === "event-participation" ? "active" : ""
                }`}
              onClick={() => setActiveTab("event-participation")}
            >
              📅 Participación en Eventos
            </button>
          )}

          {/* Pestaña Gestión de Filtros de Eventos - Para todos los roles */}
          {hasPermission("view_all") && (
            <button
              className={`tab-button ${activeTab === "event-filters" ? "active" : ""
                }`}
              onClick={() => setActiveTab("event-filters")}
            >
              🎯 Filtros de Eventos
            </button>
          )}

          {/* Pestaña GraphQL - Solo para PRESIDENTE y VOCAL */}
          {currentUser.rol &&
            (currentUser.rol.toUpperCase() === "PRESIDENTE" ||
              currentUser.rol.toUpperCase() === "VOCAL") && (
              <button
                className={`tab-button ${activeTab === "graphql-reports" ? "active" : ""
                  }`}
                onClick={() => setActiveTab("graphql-reports")}
              >
                📊 GraphQL Reports
              </button>
            )}
        </div>

        <div>
          {activeTab === "usuarios" && hasPermission("manage_users") && (
            <div>
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  marginBottom: "var(--spacing-lg)",
                  flexWrap: "wrap",
                  gap: "var(--spacing-md)",
                }}
              >
                <h2 style={{ margin: 0, color: "var(--text-primary)" }}>
                  Gestión de Usuarios
                </h2>
                <button
                  className="btn btn-primary"
                  onClick={() => handleCreate("user")}
                >
                  ➕ Nuevo Usuario
                </button>
              </div>

              <div
                style={{
                  overflowX: "auto",
                  borderRadius: "var(--border-radius-md)",
                }}
              >
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
                    {usuarios.map((u) => (
                      <tr key={u.id}>
                        <td
                          style={{
                            fontWeight: "600",
                            color: "var(--primary-color)",
                          }}
                        >
                          #{u.id}
                        </td>
                        <td>
                          <div>
                            <div style={{ fontWeight: "500" }}>
                              {u.nombreUsuario}
                            </div>
                            <div
                              style={{
                                fontSize: "0.8rem",
                                color: "var(--text-muted)",
                              }}
                            >
                              {u.email}
                            </div>
                          </div>
                        </td>
                        <td>
                          {u.nombre} {u.apellido}
                        </td>
                        <td>{u.telefono}</td>
                        <td>
                          <span className="category-tag">{u.rol}</span>
                        </td>
                        <td>
                          <span
                            className={`status-badge ${u.activo ? "status-active" : "status-inactive"
                              }`}
                          >
                            {u.activo ? "✓ Activo" : "✗ Inactivo"}
                          </span>
                        </td>
                        <td>
                          <div className="action-buttons">
                            <button
                              className="btn btn-secondary btn-sm"
                              onClick={() => handleEdit(u, "user")}
                            >
                              ✏️ Editar
                            </button>
                            <button
                              className="btn btn-danger btn-sm"
                              onClick={() => handleDelete(u.id, "user")}
                            >
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

          {activeTab === "eventos" && hasPermission("view_events") && (
            <div>
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  marginBottom: "var(--spacing-lg)",
                  flexWrap: "wrap",
                  gap: "var(--spacing-md)",
                }}
              >
                <h2 style={{ margin: 0, color: "var(--text-primary)" }}>
                  Gestión de Eventos
                </h2>
                {hasPermission("manage_events") && (
                  <button
                    className="btn btn-primary"
                    onClick={() => handleCreate("event")}
                  >
                    ➕ Nuevo Evento
                  </button>
                )}
              </div>

              <div
                style={{
                  overflowX: "auto",
                  borderRadius: "var(--border-radius-md)",
                }}
              >
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
                    {eventos.map((e) => (
                      <tr key={e.id}>
                        <td
                          style={{
                            fontWeight: "600",
                            color: "var(--primary-color)",
                          }}
                        >
                          #{e.id}
                        </td>
                        <td style={{ fontWeight: "500" }}>{e.nombre}</td>
                        <td>
                          <div
                            style={{
                              maxWidth: "300px",
                              overflow: "hidden",
                              textOverflow: "ellipsis",
                              whiteSpace: "nowrap",
                            }}
                          >
                            {e.descripcion}
                          </div>
                        </td>
                        <td>
                          <div>
                            {new Date(e.fechaHora).toLocaleDateString()}
                          </div>
                          <div
                            style={{
                              fontSize: "0.8rem",
                              color: "var(--text-muted)",
                            }}
                          >
                            {new Date(e.fechaHora).toLocaleTimeString()}
                          </div>
                        </td>
                        <td>
                          {e.miembros && e.miembros.length > 0 ? (
                            <div
                              style={{
                                display: "flex",
                                flexWrap: "wrap",
                                gap: "var(--spacing-xs)",
                              }}
                            >
                              {e.miembros.slice(0, 3).map((m) => (
                                <span
                                  key={m.id}
                                  className="category-tag"
                                  style={{ fontSize: "0.7rem" }}
                                >
                                  {m.nombre}
                                </span>
                              ))}
                              {e.miembros.length > 3 && (
                                <span
                                  className="category-tag"
                                  style={{ fontSize: "0.7rem" }}
                                >
                                  +{e.miembros.length - 3}
                                </span>
                              )}
                            </div>
                          ) : (
                            <span
                              style={{
                                color: "var(--text-muted)",
                                fontStyle: "italic",
                              }}
                            >
                              Sin miembros
                            </span>
                          )}
                        </td>
                        <td>
                          <div className="action-buttons">
                            {hasPermission("manage_events") && (
                              <>
                                <button
                                  className="btn btn-secondary btn-sm"
                                  onClick={() => handleEdit(e, "event")}
                                >
                                  ✏️ Editar
                                </button>
                                <button
                                  className="btn btn-danger btn-sm"
                                  onClick={() => handleDelete(e.id, "event")}
                                >
                                  🗑️ Eliminar
                                </button>
                              </>
                            )}
                            {hasPermission("join_events") && (
                              <button
                                className="btn btn-primary btn-sm"
                                onClick={() => handleJoinLeaveEvent(e)}
                              >
                                {e.miembros &&
                                  e.miembros.some((m) => m.id === currentUser.id)
                                  ? "🚪 Abandonar"
                                  : "➕ Unirse"}
                              </button>
                            )}
                            {hasPermission("manage_events") && (
                              <button
                                className="btn btn-secondary btn-sm"
                                onClick={() => handleManageMembers(e)}
                              >
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

          {activeTab === "donaciones" && hasPermission("manage_inventory") && (
            <div>
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  marginBottom: "var(--spacing-lg)",
                  flexWrap: "wrap",
                  gap: "var(--spacing-md)",
                }}
              >
                <h2 style={{ margin: 0, color: "var(--text-primary)" }}>
                  Gestión de Donaciones
                </h2>
                <button
                  className="btn btn-primary"
                  onClick={() => handleCreate("inventory")}
                >
                  ➕ Nueva Donación
                </button>
              </div>
              {/* Excel Report Downloader Section */}

              {(currentUser.rol &&


                (currentUser.rol.toUpperCase() === "PRESIDENTE" ||


                  currentUser.rol.toUpperCase() === "VOCAL")) && (


                  <ExcelReportDownloader currentUser={currentUser} />


                )}
              <div
                style={{
                  overflowX: "auto",
                  borderRadius: "var(--border-radius-md)",
                }}
              >
                <table className="table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>📦 Item</th>
                      <th>🏷️ Categoría</th>
                      <th>🔢 Cantidad</th>
                      <th>🕒 Fecha</th>
                      <th>📊 Estado</th>
                      <th>⚙️ Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {donaciones.map((d) => (
                      <tr key={d.id}>
                        <td
                          style={{
                            fontWeight: "600",
                            color: "var(--primary-color)",
                          }}
                        >
                          #{d.id}
                        </td>
                        <td style={{ fontWeight: "500" }}>{d.descripcion}</td>
                        <td>
                          <span className="category-tag">{d.categoria}</span>
                        </td>
                        <td>{d.cantidad}</td>
                        <td>
                          {new Date(d.fecha || Date.now()).toLocaleDateString()}
                        </td>
                        <td>
                          <span
                            className={`status-badge ${!d.eliminado ? "status-active" : "status-inactive"
                              }`}
                          >
                            {!d.eliminado ? "✓ Disponible" : "✗ No disponible"}
                          </span>
                        </td>
                        <td>
                          <div className="action-buttons">
                            <button
                              className="btn btn-secondary btn-sm"
                              onClick={() => handleEdit(d, "inventory")}
                            >
                              ✏️ Editar
                            </button>
                            <button
                              className="btn btn-danger btn-sm"
                              onClick={() => handleDelete(d.id, "inventory")}
                            >
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

          {activeTab === "solicitudes" && hasPermission("manage_requests") && (
            <div>
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  marginBottom: "var(--spacing-lg)",
                  flexWrap: "wrap",
                  gap: "var(--spacing-md)",
                }}
              >
                <h2 style={{ margin: 0, color: "var(--text-primary)" }}>
                  📋 Solicitudes de Donaciones
                </h2>
                <button
                  className="btn btn-primary"
                  onClick={() => handleCreate("request")}
                >
                  ➕ Nueva Solicitud
                </button>
              </div>

              <div
                style={{
                  overflowX: "auto",
                  borderRadius: "var(--border-radius-md)",
                }}
              >
                <table className="table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>🏢 Organización</th>
                      <th>📋 Solicitud</th>
                      <th>📝 Items</th>
                      <th>🕒 Fecha</th>
                      <th>⚙️ Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {solicitudes.map((s) => (
                      <tr key={s.requestId}>
                        <td
                          style={{
                            fontWeight: "600",
                            color: "var(--primary-color)",
                          }}
                        >
                          #{s.requestId}
                        </td>
                        <td>
                          <span className="category-tag">
                            {s.organizationId}
                          </span>
                        </td>
                        <td style={{ fontWeight: "500" }}>
                          Solicitud de {s.requestedDonations?.length || 0} items
                        </td>
                        <td>
                          <div
                            style={{
                              display: "flex",
                              flexWrap: "wrap",
                              gap: "var(--spacing-xs)",
                            }}
                          >
                            {s.requestedDonations
                              ?.slice(0, 2)
                              .map((item, idx) => (
                                <span
                                  key={idx}
                                  className="category-tag"
                                  style={{ fontSize: "0.7rem" }}
                                >
                                  {item.category}
                                </span>
                              ))}
                            {s.requestedDonations?.length > 2 && (
                              <span
                                className="category-tag"
                                style={{ fontSize: "0.7rem" }}
                              >
                                +{s.requestedDonations.length - 2} más
                              </span>
                            )}
                          </div>
                        </td>
                        <td>
                          {new Date(
                            s.createdAt || Date.now()
                          ).toLocaleDateString()}
                        </td>
                        <td>
                          <div className="action-buttons">
                            <button
                              className="btn btn-secondary btn-sm"
                              onClick={() => handleEdit(s, "request")}
                            >
                              ✏️ Ver Detalles
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

          {activeTab === "ofertas" && hasPermission("manage_offers") && (
            <div>
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  marginBottom: "var(--spacing-lg)",
                  flexWrap: "wrap",
                  gap: "var(--spacing-md)",
                }}
              >
                <h2 style={{ margin: 0, color: "var(--text-primary)" }}>
                  🎁 Ofertas de Donaciones
                </h2>
                <button
                  className="btn btn-primary"
                  onClick={() => handleCreate("offer")}
                >
                  ➕ Nueva Oferta
                </button>
              </div>

              <div
                style={{
                  overflowX: "auto",
                  borderRadius: "var(--border-radius-md)",
                }}
              >
                <table className="table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>🏢 Organización</th>
                      <th>🎁 Oferta</th>
                      <th>📝 Items</th>
                      <th>🕒 Fecha</th>
                      <th>⚙️ Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {ofertas.map((o) => (
                      <tr key={o.offerId}>
                        <td
                          style={{
                            fontWeight: "600",
                            color: "var(--primary-color)",
                          }}
                        >
                          #{o.offerId}
                        </td>
                        <td>
                          <span className="category-tag">
                            {o.organizationId}
                          </span>
                        </td>
                        <td style={{ fontWeight: "500" }}>
                          Oferta de {o.donations?.length || 0} items
                        </td>
                        <td>
                          <div
                            style={{
                              display: "flex",
                              flexWrap: "wrap",
                              gap: "var(--spacing-xs)",
                            }}
                          >
                            {o.donations?.slice(0, 2).map((item, idx) => (
                              <span
                                key={idx}
                                className="category-tag"
                                style={{ fontSize: "0.7rem" }}
                              >
                                {item.category}
                              </span>
                            ))}
                            {o.donations?.length > 2 && (
                              <span
                                className="category-tag"
                                style={{ fontSize: "0.7rem" }}
                              >
                                +{o.donations.length - 2} más
                              </span>
                            )}
                          </div>
                        </td>
                        <td>
                          {new Date(
                            o.createdAt || Date.now()
                          ).toLocaleDateString()}
                        </td>
                        <td>
                          <div className="action-buttons">
                            <button
                              className="btn btn-secondary btn-sm"
                              onClick={() => handleEdit(o, "offer")}
                            >
                              ✏️ Ver Detalles
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

          {activeTab === "transferencias" &&
            hasPermission("manage_transfers") && (
              <div>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginBottom: "var(--spacing-lg)",
                    flexWrap: "wrap",
                    gap: "var(--spacing-md)",
                  }}
                >
                  <h2 style={{ margin: 0, color: "var(--text-primary)" }}>
                    🔄 Transferencias de Donaciones
                  </h2>
                  <button
                    className="btn btn-primary"
                    onClick={() => handleCreate("transfer")}
                  >
                    ➕ Nueva Transferencia
                  </button>
                </div>

                <div
                  style={{
                    overflowX: "auto",
                    borderRadius: "var(--border-radius-md)",
                  }}
                >
                  <table className="table">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>🏢 Destinatario</th>
                        <th>📦 Items</th>
                        <th>🔢 Cantidad</th>
                        <th>🕒 Fecha</th>
                        <th>📊 Estado</th>
                        <th>⚙️ Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      {transferencias.map((t) => (
                        <tr key={t.transferId}>
                          <td
                            style={{
                              fontWeight: "600",
                              color: "var(--primary-color)",
                            }}
                          >
                            #{t.transferId}
                          </td>
                          <td>
                            <span className="category-tag">
                              {t.recipientOrgId || "N/A"}
                            </span>
                          </td>
                          <td>
                            {t.donations?.length > 0 ? (
                              <div
                                style={{
                                  display: "flex",
                                  flexWrap: "wrap",
                                  gap: "var(--spacing-xs)",
                                }}
                              >
                                {t.donations.slice(0, 2).map((item, idx) => (
                                  <span
                                    key={idx}
                                    className="category-tag"
                                    style={{ fontSize: "0.7rem" }}
                                  >
                                    {item.category}
                                  </span>
                                ))}
                                {t.donations.length > 2 && (
                                  <span
                                    className="category-tag"
                                    style={{ fontSize: "0.7rem" }}
                                  >
                                    +{t.donations.length - 2} más
                                  </span>
                                )}
                              </div>
                            ) : (
                              <span style={{ color: "var(--text-muted)" }}>
                                Sin items
                              </span>
                            )}
                          </td>
                          <td>
                            {t.donations?.reduce(
                              (total, item) =>
                                total + parseInt(item.quantity || 0),
                              0
                            ) || 0}
                          </td>
                          <td>
                            {new Date(
                              t.createdAt || Date.now()
                            ).toLocaleDateString()}
                          </td>
                          <td>
                            <span
                              className={`status-badge ${t.status === "COMPLETED"
                                ? "status-active"
                                : "status-inactive"
                                }`}
                            >
                              {t.status || "PENDING"}
                            </span>
                          </td>
                          <td>
                            <div className="action-buttons">
                              <button
                                className="btn btn-secondary btn-sm"
                                onClick={() => handleEdit(t, "transfer")}
                              >
                                ✏️ Ver Detalles
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

          {activeTab === "eventos-externos" &&
            hasPermission("manage_external_events") && (
              <div>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginBottom: "var(--spacing-lg)",
                    flexWrap: "wrap",
                    gap: "var(--spacing-md)",
                  }}
                >
                  <h2 style={{ margin: 0, color: "var(--text-primary)" }}>
                    🌍 Eventos Externos
                  </h2>
                  {hasPermission("manage_events") && (
                    <button
                      className="btn btn-primary"
                      onClick={() => handleCreate("external-event")}
                    >
                      ➕ Nuevo Evento Externo
                    </button>
                  )}
                </div>

                <ExternalEventsList
                  currentUser={currentUser}
                  onRefresh={() => fetchEventosExternos()}
                />
              </div>
            )}

          {activeTab === "soap-query" && (
            <SoapQuery currentUser={currentUser} />
          )}

          {activeTab === "event-participation" && (
            <EventParticipationReport currentUser={currentUser} />
          )}

          {activeTab === "event-filters" && (
            <EventFilterManager currentUser={currentUser} />
          )}

          {activeTab === "graphql-reports" && (
            <GraphQLDonationsExample currentUser={currentUser} />
          )}
        </div>
      </div>

      {showForm && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2 className="modal-title">
                {formType === "user" &&
                  (selectedItem ? "✏️ Editar Usuario" : "➕ Crear Usuario")}
                {formType === "event" &&
                  (selectedItem ? "✏️ Editar Evento" : "➕ Crear Evento")}
                {formType === "inventory" &&
                  (selectedItem ? "✏️ Editar Donación" : "➕ Crear Donación")}
                {formType === "request" &&
                  (selectedItem ? "✏️ Editar Solicitud" : "➕ Crear Solicitud")}
                {formType === "offer" &&
                  (selectedItem ? "✏️ Editar Oferta" : "➕ Crear Oferta")}
                {formType === "transfer" &&
                  (selectedItem
                    ? "✏️ Editar Transferencia"
                    : "➕ Crear Transferencia")}
                {formType === "external-event" &&
                  (selectedItem
                    ? "✏️ Editar Evento Externo"
                    : "➕ Crear Evento Externo")}
              </h2>
              <button className="close-button" onClick={handleFormCancel}>
                ×
              </button>
            </div>
            <div>
              {formType === "user" && (
                <UserForm
                  user={selectedItem}
                  onSuccess={handleFormSuccess}
                  onCancel={handleFormCancel}
                  currentUser={currentUser}
                />
              )}
              {formType === "event" && (
                <EventForm
                  event={selectedItem}
                  onSuccess={handleFormSuccess}
                  onCancel={handleFormCancel}
                  currentUser={currentUser}
                />
              )}
              {formType === "inventory" && (
                <InventoryForm
                  item={selectedItem}
                  onSuccess={handleFormSuccess}
                  onCancel={handleFormCancel}
                  currentUser={currentUser}
                />
              )}
              {formType === "request" && (
                <DonationRequestForm
                  request={selectedItem}
                  onSuccess={handleFormSuccess}
                  onCancel={handleFormCancel}
                  currentUser={currentUser}
                />
              )}
              {formType === "offer" && (
                <DonationOfferForm
                  offer={selectedItem}
                  onSuccess={handleFormSuccess}
                  onCancel={handleFormCancel}
                  currentUser={currentUser}
                />
              )}
              {formType === "transfer" && (
                <TransferForm
                  transfer={selectedItem}
                  onSuccess={handleFormSuccess}
                  onCancel={handleFormCancel}
                  currentUser={currentUser}
                />
              )}
              {formType === "external-event" && (
                <ExternalEventForm
                  event={selectedItem}
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
          <div className="modal-content" style={{ maxWidth: "600px" }}>
            <div className="modal-header">
              <h2 className="modal-title">
                👥 Gestionar Miembros - {selectedEvent.nombre}
              </h2>
              <button className="close-button" onClick={handleMemberModalClose}>
                ×
              </button>
            </div>
            <div style={{ padding: "var(--spacing-lg)" }}>
              <h3
                style={{
                  marginBottom: "var(--spacing-md)",
                  color: "var(--text-primary)",
                }}
              >
                Miembros Actuales
              </h3>
              {selectedEvent.miembros && selectedEvent.miembros.length > 0 ? (
                <div style={{ marginBottom: "var(--spacing-lg)" }}>
                  {selectedEvent.miembros.map((member) => (
                    <div
                      key={member.id}
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        padding: "var(--spacing-sm)",
                        border: "1px solid var(--border-color)",
                        borderRadius: "var(--border-radius-sm)",
                        marginBottom: "var(--spacing-xs)",
                        background: "var(--background-light)",
                      }}
                    >
                      <span>
                        {member.nombre} {member.apellido}
                      </span>
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
                <p style={{ color: "var(--text-muted)", fontStyle: "italic" }}>
                  No hay miembros en este evento
                </p>
              )}

              <h3
                style={{
                  marginBottom: "var(--spacing-md)",
                  color: "var(--text-primary)",
                }}
              >
                Agregar Miembro
              </h3>
              <div style={{ maxHeight: "300px", overflowY: "auto" }}>
                {usuarios
                  .filter(
                    (u) =>
                      u.activo &&
                      (!selectedEvent.miembros ||
                        !selectedEvent.miembros.some((m) => m.id === u.id))
                  )
                  .map((user) => (
                    <div
                      key={user.id}
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        padding: "var(--spacing-sm)",
                        border: "1px solid var(--border-color)",
                        borderRadius: "var(--border-radius-sm)",
                        marginBottom: "var(--spacing-xs)",
                        background: "var(--background-light)",
                      }}
                    >
                      <span>
                        {user.nombre} {user.apellido} ({user.rol})
                      </span>
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
