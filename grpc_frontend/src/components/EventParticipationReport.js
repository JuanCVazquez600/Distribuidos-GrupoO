import React, { useState, useEffect } from "react";
import { useQuery, useLazyQuery, gql } from "@apollo/client";

// Definir la consulta GraphQL para participación en eventos
const GET_EVENT_PARTICIPATION_REPORT = gql`
  query GetEventParticipationReport(
    $userId: Int!
    $filters: EventParticipationFilters!
  ) {
    eventParticipationReport(userId: $userId, filters: $filters) {
      participaciones {
        mes
        eventos {
          dia
          nombreEvento
          descripcion
          donaciones
        }
      }
    }
  }
`;

// Consulta para obtener lista de usuarios (para el filtro de usuario)
const GET_USERS = gql`
  query GetUsers {
    users {
      id
      nombre
      apellido
      rol
    }
  }
`;

const EventParticipationReport = ({ currentUser }) => {
  // Determinar si puede cambiar el filtro de usuario
  const canChangeUserFilter = () => {
    const userRole = currentUser?.rol?.toUpperCase();
    return userRole === "PRESIDENTE" || userRole === "COORDINADOR";
  };

  // Estado para los filtros
  const [filters, setFilters] = useState({
    usuarioId: currentUser?.id ? parseInt(currentUser.id) : "",
    fechaDesde: "",
    fechaHasta: "",
    repartoDonaciones: null, // null = ambos, true = sí, false = no
  });

  // Estado para lista de usuarios
  const [availableUsers, setAvailableUsers] = useState(
    !canChangeUserFilter() && currentUser?.id ? [{
      id: currentUser.id,
      nombre: currentUser.nombre,
      apellido: currentUser.apellido,
      rol: currentUser.rol
    }] : []
  );

  // Query para obtener usuarios disponibles (solo si puede cambiar el filtro)
  const { data: usersData, loading: usersLoading } = useQuery(GET_USERS, {
    skip: !currentUser?.id || !canChangeUserFilter(),
    onError: (error) => {
      console.error("Error fetching users:", error);
    },
  });

  // Efecto para cargar usuarios disponibles según permisos
  useEffect(() => {
    if (usersData?.users) {
      const userRole = currentUser?.rol?.toUpperCase();
      let filteredUsers = usersData.users;

      // Si no es PRESIDENTE o COORDINADOR, solo puede ver su propio usuario
      if (userRole !== "PRESIDENTE" && userRole !== "COORDINADOR") {
        filteredUsers = usersData.users.filter(
          (user) => user.id === currentUser.id
        );
      }

      setAvailableUsers(filteredUsers);

      // Si no hay usuario seleccionado y hay usuarios disponibles, seleccionar el primero
      if (!filters.usuarioId && filteredUsers.length > 0) {
        setFilters((prev) => ({
          ...prev,
          usuarioId: filteredUsers[0].id.toString(),
        }));
      }
    } else if (!canChangeUserFilter() && currentUser?.id) {
      // Si no puede cambiar el filtro y no hay datos de usuarios, usar el usuario actual
      setAvailableUsers([{
        id: currentUser.id,
        nombre: currentUser.nombre,
        apellido: currentUser.apellido,
        rol: currentUser.rol
      }]);
      if (!filters.usuarioId) {
        setFilters((prev) => ({
          ...prev,
          usuarioId: currentUser.id.toString(),
        }));
      }
    }
  }, [usersData, currentUser, filters.usuarioId]);

  // Query lazy para el reporte (se ejecuta manualmente)
  const [
    getReport,
    { loading: reportLoading, error: reportError, data: reportData },
  ] = useLazyQuery(GET_EVENT_PARTICIPATION_REPORT);

  // Verificar que el usuario esté logueado
  if (!currentUser || !currentUser.id) {
    return (
      <div style={{ padding: "20px", textAlign: "center" }}>
        <h3>Acceso denegado</h3>
        <p>Debe iniciar sesión para acceder a esta funcionalidad.</p>
      </div>
    );
  }

  // Función para ejecutar el reporte
  const handleGenerateReport = () => {
    if (!filters.usuarioId || isNaN(parseInt(filters.usuarioId))) {
      alert("Debe seleccionar un usuario válido para generar el reporte");
      return;
    }
    getReport({
      variables: {
        userId: currentUser?.id,
        filters: {
          usuarioId: parseInt(filters.usuarioId),
          ...(filters.fechaDesde && { fechaDesde: filters.fechaDesde }),
          ...(filters.fechaHasta && { fechaHasta: filters.fechaHasta }),
          ...(filters.repartoDonaciones !== null && {
            repartoDonaciones: filters.repartoDonaciones,
          }),
        },
      },
    });
  };

  // Función para limpiar filtros
  const handleClearFilters = () => {
    setFilters({
      usuarioId: canChangeUserFilter() ? "" : currentUser?.id?.toString() || "",
      fechaDesde: "",
      fechaHasta: "",
      repartoDonaciones: null,
    });
  };

  // Función para formatear el mes
  const formatMonth = (mes) => {
    const [year, month] = mes.split("-");
    const date = new Date(year, month - 1);
    return date.toLocaleDateString("es-ES", { year: "numeric", month: "long" });
  };

  if (usersLoading) {
    return (
      <div style={{ padding: "20px", textAlign: "center" }}>
        <h3>📅 Cargando Participación en Eventos...</h3>
        <div>⏳ Obteniendo datos...</div>
      </div>
    );
  }

  return (
    <div style={{ padding: "20px" }}>
      <h3>📅 Participación en Eventos</h3>
      <p style={{ color: "#666", marginBottom: "20px" }}>
        Consulta la participación de miembros en eventos organizados. Los
        resultados se agrupan por mes.
      </p>

      {/* Formulario de filtros */}
      <div
        style={{
          backgroundColor: "#f8f9fa",
          padding: "20px",
          borderRadius: "8px",
          marginBottom: "20px",
          border: "1px solid #dee2e6",
        }}
      >
        <h4 style={{ marginTop: 0, marginBottom: "15px" }}>🔍 Filtros</h4>
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))",
            gap: "15px",
            alignItems: "end",
          }}
        >
          {/* Filtro por usuario */}
          <div>
            <label
              style={{
                display: "block",
                marginBottom: "5px",
                fontWeight: "bold",
              }}
            >
              Usuario: <span style={{ color: "red" }}>*</span>
            </label>
            <select
              value={filters.usuarioId}
              onChange={(e) =>
                setFilters((prev) => ({
                  ...prev,
                  usuarioId: parseInt(e.target.value),
                }))
              }
              disabled={!canChangeUserFilter()}
              style={{
                width: "100%",
                padding: "8px",
                borderRadius: "4px",
                border: "1px solid #ccc",
                backgroundColor: canChangeUserFilter() ? "white" : "#f8f9fa",
              }}
            >
              <option value="">Seleccionar usuario...</option>
              {availableUsers.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.nombre} {user.apellido} ({user.rol})
                </option>
              ))}
            </select>
            {!canChangeUserFilter() && (
              <small
                style={{ color: "#666", display: "block", marginTop: "3px" }}
              >
                Solo puedes consultar tu propia participación
              </small>
            )}
          </div>

          {/* Filtro fecha desde */}
          <div>
            <label
              style={{
                display: "block",
                marginBottom: "5px",
                fontWeight: "bold",
              }}
            >
              Fecha desde:
            </label>
            <input
              type="date"
              value={filters.fechaDesde}
              onChange={(e) =>
                setFilters((prev) => ({ ...prev, fechaDesde: e.target.value }))
              }
              style={{
                width: "100%",
                padding: "8px",
                borderRadius: "4px",
                border: "1px solid #ccc",
              }}
            />
          </div>

          {/* Filtro fecha hasta */}
          <div>
            <label
              style={{
                display: "block",
                marginBottom: "5px",
                fontWeight: "bold",
              }}
            >
              Fecha hasta:
            </label>
            <input
              type="date"
              value={filters.fechaHasta}
              onChange={(e) =>
                setFilters((prev) => ({ ...prev, fechaHasta: e.target.value }))
              }
              style={{
                width: "100%",
                padding: "8px",
                borderRadius: "4px",
                border: "1px solid #ccc",
              }}
            />
          </div>

          {/* Filtro por reparto de donaciones */}
          <div>
            <label
              style={{
                display: "block",
                marginBottom: "5px",
                fontWeight: "bold",
              }}
            >
              Reparto de donaciones:
            </label>
            <select
              value={
                filters.repartoDonaciones === null
                  ? "ambos"
                  : filters.repartoDonaciones
                  ? "si"
                  : "no"
              }
              onChange={(e) => {
                const value =
                  e.target.value === "ambos" ? null : e.target.value === "si";
                setFilters((prev) => ({ ...prev, repartoDonaciones: value }));
              }}
              style={{
                width: "100%",
                padding: "8px",
                borderRadius: "4px",
                border: "1px solid #ccc",
              }}
            >
              <option value="ambos">Ambos</option>
              <option value="si">Solo con donaciones</option>
              <option value="no">Solo sin donaciones</option>
            </select>
          </div>

          {/* Botones de acción */}
          <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
            <button
              onClick={handleGenerateReport}
              disabled={!filters.usuarioId || reportLoading}
              style={{
                backgroundColor: "#28a745",
                color: "white",
                border: "none",
                padding: "8px 16px",
                borderRadius: "4px",
                cursor:
                  !filters.usuarioId || reportLoading
                    ? "not-allowed"
                    : "pointer",
                fontWeight: "bold",
                opacity: !filters.usuarioId || reportLoading ? 0.6 : 1,
              }}
            >
              {reportLoading ? "⏳ Generando..." : "📊 Generar Reporte"}
            </button>
            <button
              onClick={handleClearFilters}
              style={{
                backgroundColor: "#6c757d",
                color: "white",
                border: "none",
                padding: "8px 16px",
                borderRadius: "4px",
                cursor: "pointer",
              }}
            >
              🗑️ Limpiar
            </button>
          </div>
        </div>
      </div>

      {/* Mensaje de error */}
      {reportError && (
        <div
          style={{
            backgroundColor: "#f8d7da",
            color: "#721c24",
            padding: "15px",
            borderRadius: "4px",
            marginBottom: "20px",
            border: "1px solid #f5c6cb",
          }}
        >
          <strong>❌ Error al generar el reporte:</strong> {reportError.message}
          {(() => {
            console.log("GraphQL Error:", reportError);
            console.log(
              "Error instanceof Error:",
              reportError instanceof Error
            );
            console.log("Error details:", {
              message: reportError.message,
              graphQLErrors: reportError.graphQLErrors,
              networkError: reportError.networkError,
              extraInfo: reportError.extraInfo,
            });
            return null;
          })()}
        </div>
      )}

      {/* Resultados del reporte */}
      {reportData?.eventParticipationReport && (
        <div>
          <h4>📋 Resultados de Participación</h4>

          {/* Indicador de filtros activos */}
          {(filters.fechaDesde ||
            filters.fechaHasta ||
            filters.repartoDonaciones !== null) && (
            <div
              style={{
                backgroundColor: "#e7f3ff",
                padding: "10px",
                borderRadius: "6px",
                marginBottom: "15px",
                border: "1px solid #b3d9ff",
              }}
            >
              <strong>🔍 Filtros aplicados:</strong>
              {filters.fechaDesde && (
                <span style={{ marginLeft: "10px" }}>
                  📅 Desde: {filters.fechaDesde}
                </span>
              )}
              {filters.fechaHasta && (
                <span style={{ marginLeft: "10px" }}>
                  📅 Hasta: {filters.fechaHasta}
                </span>
              )}
              {filters.repartoDonaciones !== null && (
                <span style={{ marginLeft: "10px" }}>
                  {filters.repartoDonaciones
                    ? "✅ Solo con donaciones"
                    : "❌ Solo sin donaciones"}
                </span>
              )}
            </div>
          )}

          {reportData.eventParticipationReport.participaciones.length === 0 ? (
            <div
              style={{
                backgroundColor: "#fff3cd",
                color: "#856404",
                padding: "20px",
                borderRadius: "8px",
                textAlign: "center",
                border: "1px solid #ffeaa7",
              }}
            >
              <h5>📭 No se encontraron resultados</h5>
              <p>
                No hay participación registrada para los filtros seleccionados.
              </p>
            </div>
          ) : (
            <div>
              {reportData.eventParticipationReport.participaciones.map(
                (participacion, index) => (
                  <div
                    key={participacion.mes}
                    style={{
                      backgroundColor: "white",
                      border: "1px solid #dee2e6",
                      borderRadius: "8px",
                      marginBottom: "20px",
                      overflow: "hidden",
                    }}
                  >
                    <div
                      style={{
                        backgroundColor: "#007bff",
                        color: "white",
                        padding: "15px",
                        fontSize: "1.2rem",
                        fontWeight: "bold",
                      }}
                    >
                      📅 {formatMonth(participacion.mes)}
                    </div>

                    <div style={{ padding: "0" }}>
                      {participacion.eventos.map((evento, eventIndex) => (
                        <div
                          key={eventIndex}
                          style={{
                            padding: "15px",
                            borderBottom:
                              eventIndex < participacion.eventos.length - 1
                                ? "1px solid #eee"
                                : "none",
                            display: "grid",
                            gridTemplateColumns: "1fr 2fr 1fr",
                            gap: "15px",
                            alignItems: "start",
                          }}
                        >
                          <div>
                            <strong style={{ color: "#007bff" }}>
                              📆 {evento.dia}
                            </strong>
                          </div>

                          <div>
                            <div
                              style={{
                                fontWeight: "bold",
                                marginBottom: "5px",
                              }}
                            >
                              🎪 {evento.nombreEvento}
                            </div>
                            <div style={{ color: "#666", fontSize: "0.9rem" }}>
                              {evento.descripcion}
                            </div>
                          </div>

                          <div>
                            <strong>📦 Donaciones:</strong>
                            <div
                              style={{
                                marginTop: "5px",
                                fontSize: "0.9rem",
                                color: evento.donaciones
                                  ? "#28a745"
                                  : "#6c757d",
                              }}
                            >
                              {evento.donaciones ||
                                "Sin donaciones registradas"}
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )
              )}
            </div>
          )}
        </div>
      )}

      {/* Mensaje inicial */}
      {!reportData?.eventParticipationReport &&
        !reportLoading &&
        !reportError && (
          <div
            style={{
              backgroundColor: "#e9ecef",
              padding: "30px",
              borderRadius: "8px",
              textAlign: "center",
              border: "1px solid #ced4da",
            }}
          >
            <h4>🎯 Genera tu Reporte</h4>
            <p>
              Selecciona los filtros deseados y haz clic en "Generar Reporte"
              para ver la participación en eventos.
            </p>
            <p style={{ fontSize: "0.9rem", color: "#666" }}>
              Recuerda que el filtro de usuario es obligatorio.
            </p>
          </div>
        )}
    </div>
  );
};

export default EventParticipationReport;
