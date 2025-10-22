import React, { useState } from "react";
import axios from "axios";

const SoapQuery = ({ currentUser }) => {
  const [orgIds, setOrgIds] = useState("");
  const [presidentsData, setPresidentsData] = useState([]);
  const [associationsData, setAssociationsData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handlePresidentsQuery = async () => {
    if (!orgIds.trim()) {
      setError("Por favor ingresa IDs de organizaciones");
      return;
    }

    setLoading(true);
    setError("");
    try {
      const response = await axios.post(
        "http://localhost:5000/api/soap/presidents",
        {
          userId: currentUser?.id,
          orgIds: orgIds,
        }
      );
      setPresidentsData(response.data.data);
      setAssociationsData([]); // Limpiar datos de asociaciones
    } catch (err) {
      const errorMessage =
        err.response?.data?.error || "Error al consultar presidentes";
      setError(errorMessage);
      console.error("Error en consulta de presidentes:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleAssociationsQuery = async () => {
    if (!orgIds.trim()) {
      setError("Por favor ingresa IDs de organizaciones");
      return;
    }

    setLoading(true);
    setError("");
    try {
      const response = await axios.post(
        "http://localhost:5000/api/soap/associations",
        {
          userId: currentUser?.id,
          orgIds: orgIds,
        }
      );
      setAssociationsData(response.data.data);
      setPresidentsData([]); // Limpiar datos de presidentes
    } catch (err) {
      const errorMessage =
        err.response?.data?.error || "Error al consultar organizaciones";
      setError(errorMessage);
      console.error("Error en consulta de organizaciones:", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        padding: "var(--spacing-lg)",
        maxWidth: "1200px",
        margin: "0 auto",
      }}
    >
      <div
        style={{
          marginBottom: "var(--spacing-2xl)",
          textAlign: "center",
        }}
      >
        <h2
          style={{
            marginBottom: "var(--spacing-md)",
            color: "var(--text-primary)",
            fontSize: "1.8rem",
            fontWeight: "600",
          }}
        >
          🧼 Consulta SOAP - Presidentes y Organizaciones
        </h2>
        <p
          style={{
            color: "var(--text-secondary)",
            fontSize: "1rem",
            margin: 0,
          }}
        >
          Consulta datos de presidentes y organizaciones de la red de ONGs
        </p>
      </div>

      <div
        style={{
          background: "var(--background-light)",
          padding: "var(--spacing-xl)",
          borderRadius: "var(--border-radius-lg)",
          boxShadow: "var(--shadow-md)",
          marginBottom: "var(--spacing-xl)",
        }}
      >
        <div
          style={{
            marginBottom: "var(--spacing-lg)",
          }}
        >
          <label
            htmlFor="orgIds"
            style={{
              display: "block",
              marginBottom: "var(--spacing-sm)",
              fontWeight: "500",
              color: "var(--text-primary)",
              fontSize: "1rem",
            }}
          >
            IDs de Organizaciones (separados por coma):
          </label>
          <input
            type="text"
            id="orgIds"
            value={orgIds}
            onChange={(e) => setOrgIds(e.target.value)}
            placeholder="Ej: 6,5,8,10"
            style={{
              width: "100%",
              padding: "var(--spacing-md)",
              border: "1px solid var(--border-color)",
              borderRadius: "var(--border-radius-md)",
              fontSize: "1rem",
              background: "white",
              color: "var(--text-primary)",
              boxSizing: "border-box",
            }}
          />
        </div>

        <div
          style={{
            display: "flex",
            gap: "var(--spacing-md)",
            flexWrap: "wrap",
          }}
        >
          <button
            onClick={handlePresidentsQuery}
            disabled={loading}
            style={{
              padding: "var(--spacing-md) var(--spacing-xl)",
              background: loading
                ? "var(--text-muted)"
                : "var(--primary-color)",
              color: "white",
              border: "none",
              borderRadius: "var(--border-radius-md)",
              cursor: loading ? "not-allowed" : "pointer",
              fontSize: "1rem",
              fontWeight: "500",
              transition: "all 0.2s ease",
              flex: "1",
              minWidth: "200px",
            }}
          >
            {loading ? "🔄 Consultando..." : "👥 Consultar Presidentes"}
          </button>

          <button
            onClick={handleAssociationsQuery}
            disabled={loading}
            style={{
              padding: "var(--spacing-md) var(--spacing-xl)",
              background: loading ? "var(--text-muted)" : "var(--accent-color)",
              color: "white",
              border: "none",
              borderRadius: "var(--border-radius-md)",
              cursor: loading ? "not-allowed" : "pointer",
              fontSize: "1rem",
              fontWeight: "500",
              transition: "all 0.2s ease",
              flex: "1",
              minWidth: "200px",
            }}
          >
            {loading ? "🔄 Consultando..." : "🏢 Consultar Organizaciones"}
          </button>
        </div>
      </div>

      {error && (
        <div
          style={{
            background: "var(--danger-color)",
            color: "white",
            padding: "var(--spacing-md)",
            borderRadius: "var(--border-radius-md)",
            marginBottom: "var(--spacing-lg)",
            display: "flex",
            alignItems: "center",
            gap: "var(--spacing-sm)",
          }}
        >
          <span>⚠️</span>
          <span>{error}</span>
        </div>
      )}

      {/* Tabla de Presidentes */}
      {presidentsData.length > 0 && (
        <div
          style={{
            background: "var(--background-light)",
            padding: "var(--spacing-xl)",
            borderRadius: "var(--border-radius-lg)",
            boxShadow: "var(--shadow-md)",
            marginBottom: "var(--spacing-xl)",
          }}
        >
          <h3
            style={{
              marginBottom: "var(--spacing-lg)",
              color: "var(--text-primary)",
              fontSize: "1.4rem",
              fontWeight: "600",
              display: "flex",
              alignItems: "center",
              gap: "var(--spacing-sm)",
            }}
          >
            👥 Presidentes Encontrados ({presidentsData.length})
          </h3>
          <div
            style={{
              overflowX: "auto",
              borderRadius: "var(--border-radius-md)",
            }}
          >
            <table
              style={{
                width: "100%",
                borderCollapse: "collapse",
                background: "white",
                borderRadius: "var(--border-radius-md)",
                overflow: "hidden",
                boxShadow: "var(--shadow-sm)",
              }}
            >
              <thead>
                <tr
                  style={{
                    background: "var(--primary-color)",
                    color: "white",
                  }}
                >
                  <th
                    style={{
                      padding: "var(--spacing-md)",
                      textAlign: "left",
                      fontWeight: "600",
                    }}
                  >
                    ID
                  </th>
                  <th
                    style={{
                      padding: "var(--spacing-md)",
                      textAlign: "left",
                      fontWeight: "600",
                    }}
                  >
                    👤 Nombre
                  </th>
                  <th
                    style={{
                      padding: "var(--spacing-md)",
                      textAlign: "left",
                      fontWeight: "600",
                    }}
                  >
                    📍 Dirección
                  </th>
                  <th
                    style={{
                      padding: "var(--spacing-md)",
                      textAlign: "left",
                      fontWeight: "600",
                    }}
                  >
                    📞 Teléfono
                  </th>
                  <th
                    style={{
                      padding: "var(--spacing-md)",
                      textAlign: "left",
                      fontWeight: "600",
                    }}
                  >
                    🏢 ID Organización
                  </th>
                </tr>
              </thead>
              <tbody>
                {presidentsData.map((president, index) => (
                  <tr
                    key={index}
                    style={{
                      borderBottom: "1px solid var(--border-color)",
                      background:
                        index % 2 === 0 ? "white" : "var(--background-light)",
                    }}
                  >
                    <td
                      style={{
                        padding: "var(--spacing-md)",
                        fontWeight: "600",
                        color: "var(--primary-color)",
                      }}
                    >
                      #{president.id}
                    </td>
                    <td style={{ padding: "var(--spacing-md)" }}>
                      {president.name}
                    </td>
                    <td style={{ padding: "var(--spacing-md)" }}>
                      {president.address}
                    </td>
                    <td style={{ padding: "var(--spacing-md)" }}>
                      {president.phone}
                    </td>
                    <td style={{ padding: "var(--spacing-md)" }}>
                      <span
                        style={{
                          background: "var(--accent-color)",
                          color: "white",
                          padding: "var(--spacing-xs) var(--spacing-sm)",
                          borderRadius: "var(--border-radius-sm)",
                          fontSize: "0.9rem",
                          fontWeight: "500",
                        }}
                      >
                        #{president.organization_id}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tabla de Organizaciones */}
      {associationsData.length > 0 && (
        <div
          style={{
            background: "var(--background-light)",
            padding: "var(--spacing-xl)",
            borderRadius: "var(--border-radius-lg)",
            boxShadow: "var(--shadow-md)",
          }}
        >
          <h3
            style={{
              marginBottom: "var(--spacing-lg)",
              color: "var(--text-primary)",
              fontSize: "1.4rem",
              fontWeight: "600",
              display: "flex",
              alignItems: "center",
              gap: "var(--spacing-sm)",
            }}
          >
            🏢 Organizaciones Encontradas ({associationsData.length})
          </h3>
          <div
            style={{
              overflowX: "auto",
              borderRadius: "var(--border-radius-md)",
            }}
          >
            <table
              style={{
                width: "100%",
                borderCollapse: "collapse",
                background: "white",
                borderRadius: "var(--border-radius-md)",
                overflow: "hidden",
                boxShadow: "var(--shadow-sm)",
              }}
            >
              <thead>
                <tr
                  style={{
                    background: "var(--accent-color)",
                    color: "white",
                  }}
                >
                  <th
                    style={{
                      padding: "var(--spacing-md)",
                      textAlign: "left",
                      fontWeight: "600",
                    }}
                  >
                    ID
                  </th>
                  <th
                    style={{
                      padding: "var(--spacing-md)",
                      textAlign: "left",
                      fontWeight: "600",
                    }}
                  >
                    🏢 Nombre
                  </th>
                  <th
                    style={{
                      padding: "var(--spacing-md)",
                      textAlign: "left",
                      fontWeight: "600",
                    }}
                  >
                    📍 Dirección
                  </th>
                  <th
                    style={{
                      padding: "var(--spacing-md)",
                      textAlign: "left",
                      fontWeight: "600",
                    }}
                  >
                    📞 Teléfono
                  </th>
                </tr>
              </thead>
              <tbody>
                {associationsData.map((association, index) => (
                  <tr
                    key={index}
                    style={{
                      borderBottom: "1px solid var(--border-color)",
                      background:
                        index % 2 === 0 ? "white" : "var(--background-light)",
                    }}
                  >
                    <td
                      style={{
                        padding: "var(--spacing-md)",
                        fontWeight: "600",
                        color: "var(--primary-color)",
                      }}
                    >
                      #{association.id}
                    </td>
                    <td style={{ padding: "var(--spacing-md)" }}>
                      {association.name}
                    </td>
                    <td style={{ padding: "var(--spacing-md)" }}>
                      {association.address}
                    </td>
                    <td style={{ padding: "var(--spacing-md)" }}>
                      {association.phone}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default SoapQuery;
