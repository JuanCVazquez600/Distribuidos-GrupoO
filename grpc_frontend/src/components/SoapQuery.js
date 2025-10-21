import React, { useState } from "react";
import axios from "axios";

const SoapQuery = () => {
  const [orgIds, setOrgIds] = useState("");
  const [presidentsData, setPresidentsData] = useState([]);
  const [associationsData, setAssociationsData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // Obtener userId del localStorage (asumiendo que está guardado después del login)
  const userId = localStorage.getItem("userId");

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
          userId: parseInt(userId),
          orgIds: orgIds,
        }
      );
      setPresidentsData(response.data.data);
      setAssociationsData([]); // Limpiar datos de asociaciones
    } catch (err) {
      setError(err.response?.data?.error || "Error al consultar presidentes");
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
          userId: parseInt(userId),
          orgIds: orgIds,
        }
      );
      setAssociationsData(response.data.data);
      setPresidentsData([]); // Limpiar datos de presidentes
    } catch (err) {
      setError(
        err.response?.data?.error || "Error al consultar organizaciones"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="soap-query-container">
      <h2>Consulta SOAP - Presidentes y Organizaciones</h2>

      <div className="query-form">
        <div className="form-group">
          <label htmlFor="orgIds">
            IDs de Organizaciones (separados por coma):
          </label>
          <input
            type="text"
            id="orgIds"
            value={orgIds}
            onChange={(e) => setOrgIds(e.target.value)}
            placeholder="Ej: 6,5,8,10"
            className="form-control"
          />
        </div>

        <div className="button-group">
          <button
            onClick={handlePresidentsQuery}
            disabled={loading}
            className="btn btn-primary"
          >
            {loading ? "Consultando..." : "Consultar Presidentes"}
          </button>

          <button
            onClick={handleAssociationsQuery}
            disabled={loading}
            className="btn btn-secondary"
          >
            {loading ? "Consultando..." : "Consultar Organizaciones"}
          </button>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {/* Tabla de Presidentes */}
      {presidentsData.length > 0 && (
        <div className="results-section">
          <h3>Presidentes Encontrados</h3>
          <div className="table-responsive">
            <table className="table table-striped">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nombre</th>
                  <th>Dirección</th>
                  <th>Teléfono</th>
                  <th>ID Organización</th>
                </tr>
              </thead>
              <tbody>
                {presidentsData.map((president, index) => (
                  <tr key={index}>
                    <td>{president.id}</td>
                    <td>{president.name}</td>
                    <td>{president.address}</td>
                    <td>{president.phone}</td>
                    <td>{president.organization_id}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tabla de Organizaciones */}
      {associationsData.length > 0 && (
        <div className="results-section">
          <h3>Organizaciones Encontradas</h3>
          <div className="table-responsive">
            <table className="table table-striped">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nombre</th>
                  <th>Dirección</th>
                  <th>Teléfono</th>
                </tr>
              </thead>
              <tbody>
                {associationsData.map((association, index) => (
                  <tr key={index}>
                    <td>{association.id}</td>
                    <td>{association.name}</td>
                    <td>{association.address}</td>
                    <td>{association.phone}</td>
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
