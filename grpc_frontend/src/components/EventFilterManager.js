/* eslint-disable no-unused-vars */
  import React, { useState, useEffect } from "react";
import axios from "axios";

const EventFilterManager = ({ currentUser }) => {
  // Estado para filtros guardados
  const [filtros, setFiltros] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingFilter, setEditingFilter] = useState(null);
  const [filterForm, setFilterForm] = useState({
    nombre: "",
    fechaInicio: "",
    fechaFin: "",
    nombreEvento: "",
    minParticipantes: "",
    maxParticipantes: "",
  });

  // Estado para filtros dinámicos y resultados
  const [dynamicFilters, setDynamicFilters] = useState({
    fechaInicio: "",
    fechaFin: "",
    nombreEvento: "",
    minParticipantes: "",
    maxParticipantes: "",
  });
  const [filteredEvents, setFilteredEvents] = useState([]);
  const [loadingResults, setLoadingResults] = useState(false);
  const [showSavedFilters, setShowSavedFilters] = useState(false);

  // Cargar filtros guardados
  const fetchFiltros = async () => {
    try {
      setLoading(true);
      const response = await axios.get(
        `http://localhost:8082/api/filtros/usuario/${currentUser.id}`
      );

      // Parse filtrosJson (stored as a string) and merge into the filter object so
      // the UI can read properties like fechaInicio, fechaFin, nombreEvento, minParticipantes, maxParticipantes
      const parsed = (response.data || []).map(f => {
        try {
          const parsedJson = f.filtrosJson ? JSON.parse(f.filtrosJson) : {};
          return {
            ...f,
            fechaInicio: parsedJson.fechaInicio || parsedJson.fecha_inicio || f.fechaInicio || "",
            fechaFin: parsedJson.fechaFin || parsedJson.fecha_fin || f.fechaFin || "",
            nombreEvento: parsedJson.nombreEvento || parsedJson.nombre_evento || f.nombreEvento || "",
            minParticipantes: parsedJson.minParticipantes != null ? parsedJson.minParticipantes : (parsedJson.min_participantes != null ? parsedJson.min_participantes : f.minParticipantes),
            maxParticipantes: parsedJson.maxParticipantes != null ? parsedJson.maxParticipantes : (parsedJson.max_participantes != null ? parsedJson.max_participantes : f.maxParticipantes),
          };
        } catch (e) {
          // If parsing fails, return the raw filter object
          console.warn('No se pudo parsear filtrosJson para el filtro', f.id, e);
          return f;
        }
      });

  console.log('Filtros obtenidos y parseados:', parsed);
  setFiltros(parsed);
    } catch (error) {
      console.error("Error fetching filtros:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFiltros();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentUser.id]);

  // Guardar filtro
  const handleSaveFilter = async () => {
    if (!filterForm.nombre.trim()) {
      alert("El nombre del filtro es obligatorio");
      return;
    }

    try {
      const filterData = {
        // DTO shape expected by backend: include filtrosJson as string
        nombre: filterForm.nombre,
        usuarioId: currentUser.id,
        tipoFiltro: "EVENTO_PARTICIPACION",
        filtrosJson: JSON.stringify({
          fechaInicio: filterForm.fechaInicio || null,
          fechaFin: filterForm.fechaFin || null,
          nombreEvento: filterForm.nombreEvento || null,
          minParticipantes: filterForm.minParticipantes ? parseInt(filterForm.minParticipantes) : null,
          maxParticipantes: filterForm.maxParticipantes ? parseInt(filterForm.maxParticipantes) : null,
        }),
      };

      if (editingFilter) {
        await axios.put(
          `http://localhost:8082/api/filtros/${editingFilter.id}`,
          filterData
        );
      } else {
        await axios.post("http://localhost:8082/api/filtros", filterData);
      }

      fetchFiltros();
      setShowForm(false);
      setEditingFilter(null);
      setFilterForm({
        nombre: "",
        fechaInicio: "",
        fechaFin: "",
        nombreEvento: "",
        minParticipantes: "",
        maxParticipantes: "",
      });
    } catch (error) {
      console.error("Error saving filter:", error);
      alert("Error al guardar el filtro");
    }
  };

  // Eliminar filtro
  const handleDeleteFilter = async (filterId) => {
    if (!window.confirm("¿Estás seguro de que quieres eliminar este filtro?")) {
      return;
    }

    try {
      // Backend delete endpoint requires usuarioId in path: /api/filtros/{id}/usuario/{usuarioId}
      await axios.delete(`http://localhost:8082/api/filtros/${filterId}/usuario/${currentUser.id}`);
      await fetchFiltros();
    } catch (error) {
      console.error("Error deleting filter:", error);
      alert("Error al eliminar el filtro: " + (error.response?.data || error.message));
    }
  };

  // Editar filtro
  const handleEditFilter = (filter) => {
    setEditingFilter(filter);
    setFilterForm({
      nombre: filter.nombre,
      fechaInicio: filter.fechaInicio || "",
      fechaFin: filter.fechaFin || "",
      nombreEvento: filter.nombreEvento || "",
      minParticipantes: filter.minParticipantes || "",
      maxParticipantes: filter.maxParticipantes || "",
    });
    setShowForm(true);
  };

  // Aplicar filtros dinámicos
  const applyDynamicFilters = async () => {
    try {
      setLoadingResults(true);
      const filtrosAplicar = {};
      if (dynamicFilters.fechaInicio) filtrosAplicar.fechaInicio = dynamicFilters.fechaInicio + "T00:00:00";
      if (dynamicFilters.fechaFin) filtrosAplicar.fechaFin = dynamicFilters.fechaFin + "T23:59:59";
      if (dynamicFilters.nombreEvento) filtrosAplicar.nombreEvento = dynamicFilters.nombreEvento;
      // Establecer minParticipantes a 0 si no se especifica
      filtrosAplicar.minParticipantes = dynamicFilters.minParticipantes ? parseInt(dynamicFilters.minParticipantes) : 0;
      // Establecer maxParticipantes a un valor alto si no se especifica
      filtrosAplicar.maxParticipantes = dynamicFilters.maxParticipantes ? parseInt(dynamicFilters.maxParticipantes) : 999999;

      console.log("Aplicando filtros:", filtrosAplicar); // Debug log

      const response = await axios.post(
        "http://localhost:8082/api/eventos/reporte/participacion/filtrar",
        filtrosAplicar
      );

      console.log("Respuesta del servidor:", response.data); // Debug log
      setFilteredEvents(response.data);
    } catch (error) {
      console.error("Error applying filters:", error);
      alert("Error al aplicar los filtros: " + (error.response?.data?.message || error.message));
    } finally {
      setLoadingResults(false);
    }
  };

  // Cargar filtro guardado en filtros dinámicos
  const normalizeFilterForUI = (raw) => {
    let obj = raw || {};
    try {
      if (typeof obj === 'string') obj = JSON.parse(obj);
      if (obj.filtrosJson && typeof obj.filtrosJson === 'string') {
        const parsed = JSON.parse(obj.filtrosJson);
        obj = { ...obj, ...parsed };
      }
      if (obj.filtros && typeof obj.filtros === 'object') obj = { ...obj, ...obj.filtros };
      if (obj.filters && typeof obj.filters === 'object') obj = { ...obj, ...obj.filters };
    } catch (e) {
      console.warn('normalizeFilterForUI: parsing error', e, raw);
    }

    const get = (keys) => {
      for (const k of keys) if (obj[k] !== undefined) return obj[k];
      return undefined;
    };

    const fechaInicio = get(['fechaInicio', 'fecha_inicio', 'startDate', 'start_date']);
    const fechaFin = get(['fechaFin', 'fecha_fin', 'endDate', 'end_date']);
    const nombreEvento = get(['nombreEvento', 'nombre_evento', 'eventName', 'evento']);
    const minParticipantes = get(['minParticipantes', 'min_participantes', 'minParticipants']);
    const maxParticipantes = get(['maxParticipantes', 'max_participantes', 'maxParticipants']);

    const asDateInput = (v) => {
      if (v == null || v === '') return '';
      const s = String(v);
      if (s.includes('T')) return s.split('T')[0];
      if (s.includes(' ')) return s.split(' ')[0];
      if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s;
      const dt = new Date(s);
      if (!isNaN(dt.getTime())) {
        const y = dt.getFullYear();
        const m = String(dt.getMonth() + 1).padStart(2, '0');
        const d = String(dt.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
      }
      return s.slice(0, 10);
    };

    return {
      fechaInicio: asDateInput(fechaInicio),
      fechaFin: asDateInput(fechaFin),
      nombreEvento: nombreEvento || '',
      minParticipantes: (minParticipantes != null) ? String(minParticipantes) : '',
      maxParticipantes: (maxParticipantes != null) ? String(maxParticipantes) : '',
    };
  };

  const handleLoadSavedFilter = async (filterIdOrObj) => {
    try {
      // If passed an object with id, prefer fetching fresh copy from backend by id
      const id = typeof filterIdOrObj === 'object' ? filterIdOrObj.id : filterIdOrObj;
      console.log('Solicitando filtro desde backend id=', id);
      const response = await axios.get(`http://localhost:8082/api/filtros/${id}`);
      const filtroDTO = response.data;
      console.log('Filtro obtenido del backend:', filtroDTO);

      // Normalize and apply
      const normalized = normalizeFilterForUI(filtroDTO);
      console.log('Filtro normalizado para UI:', normalized);
      // If the saved filter doesn't contain parameters (filtrosJson was null),
      // offer to edit & save parameters so future loads populate correctly.
      const hasParams = (normalized.fechaInicio || normalized.fechaFin || normalized.nombreEvento || normalized.minParticipantes || normalized.maxParticipantes);
      if (!hasParams) {
        if (window.confirm('Este filtro no tiene parámetros guardados. ¿Quieres editarlo ahora para agregar los parámetros y guardarlo?')) {
          // Open edit modal prefilled with current normalized values (empty) so user can add params and save
          setEditingFilter(filtroDTO);
          setFilterForm({
            nombre: filtroDTO.nombre || '',
            fechaInicio: normalized.fechaInicio || '',
            fechaFin: normalized.fechaFin || '',
            nombreEvento: normalized.nombreEvento || '',
            minParticipantes: normalized.minParticipantes || '',
            maxParticipantes: normalized.maxParticipantes || '',
          });
          setShowForm(true);
        } else {
          // nothing to apply
        }
      } else {
        setDynamicFilters(normalized);
        // Optionally, focus on Apply button or keep UI visible
        setShowSavedFilters(false);
      }
    } catch (e) {
      console.error('Error cargando filtro desde backend:', e);
      // Fallback: if caller passed the filter object, try normalizing that instead of failing
      if (typeof filterIdOrObj === 'object' && filterIdOrObj != null) {
        try {
          console.warn('Intentando usar el objeto pasado como fallback para cargar valores...');
          const normalized = normalizeFilterForUI(filterIdOrObj);
          console.log('Filtro normalizado desde objeto pasado:', normalized);
          setDynamicFilters(normalized);
          setShowSavedFilters(false);
          return;
        } catch (e2) {
          console.error('Fallback also failed:', e2);
        }
      }

      alert('No se pudo cargar el filtro desde el servidor. Revisa la consola para más detalles.');
    }
  };

  // Calcular estadísticas
  const calculateStats = () => {
    const totalEvents = filteredEvents.length;
    const totalParticipants = filteredEvents.reduce((sum, event) => sum + event.numeroParticipantes, 0);
    const avgParticipants = totalEvents > 0 ? Math.round(totalParticipants / totalEvents) : 0;

    return { totalEvents, totalParticipants, avgParticipants };
  };

  // Crear reporte agrupado por rangos de participantes
  const createGroupedReport = () => {
    const groups = {
      "1-5": { count: 0, totalParticipants: 0 },
      "6-10": { count: 0, totalParticipants: 0 },
      "11-20": { count: 0, totalParticipants: 0 },
      "21+": { count: 0, totalParticipants: 0 },
    };

    filteredEvents.forEach(event => {
      const participants = event.numeroParticipantes;
      if (participants <= 5) groups["1-5"].count++;
      else if (participants <= 10) groups["6-10"].count++;
      else if (participants <= 20) groups["11-20"].count++;
      else groups["21+"].count++;

      if (participants <= 5) groups["1-5"].totalParticipants += participants;
      else if (participants <= 10) groups["6-10"].totalParticipants += participants;
      else if (participants <= 20) groups["11-20"].totalParticipants += participants;
      else groups["21+"].totalParticipants += participants;
    });

    return groups;
  };

  // Cancelar formulario
  const handleCancel = () => {
    setShowForm(false);
    setEditingFilter(null);
    setFilterForm({
      nombre: "",
      fechaInicio: "",
      fechaFin: "",
      nombreEvento: "",
      minParticipantes: "",
      maxParticipantes: "",
    });
  };

  if (loading) {
    return (
      <div style={{ padding: "20px", textAlign: "center" }}>
        <h3>🔄 Cargando Filtros...</h3>
        <div>⏳ Obteniendo filtros guardados...</div>
      </div>
    );
  }

  return (
    <div className="container" style={{ paddingTop: '20px', paddingBottom: '20px' }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "20px" }}>
        <div style={{ flex: 1, textAlign: 'center' }}>
          <h3 style={{ margin: 0 }}>🎯 Gestión de Filtros de Eventos</h3>
          <p style={{ color: "#666", margin: "5px 0" }}>
            Crea, guarda y aplica filtros personalizados para reportes de participación en eventos.
          </p>
        </div>
  <button onClick={() => setShowForm(true)} className="btn btn-primary">➕ Nuevo Filtro</button>
      </div>

      {/* Lista de filtros */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))", gap: "15px" }}>
        {filtros.map((filtro) => (
          <div key={filtro.id} className="card">
            <h4 className="card-title">{filtro.nombre}</h4>

            <div style={{ fontSize: "0.9rem", color: "#666", marginBottom: "15px" }}>
                    {/** Mostrar resumen compacto de filtros en una sola línea */}
                    {(() => {
                      const parts = [];
                      const formatDate = (d) => {
                        if (!d) return d;
                        const s = String(d);
                        // If date is in YYYY-MM-DD (no timezone) parse into local date to avoid UTC shift
                        if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
                          const [yy, mm, dd] = s.split('-').map(Number);
                          const dt = new Date(yy, mm - 1, dd); // local midnight
                          if (!isNaN(dt.getTime())) return dt.toLocaleDateString();
                        }
                        try {
                          const dt = new Date(d);
                          if (!isNaN(dt.getTime())) return dt.toLocaleDateString();
                        } catch (e) {
                          // fallback
                        }
                        return s.slice(0, 10);
                      };

                      if (filtro.fechaInicio) parts.push(`Desde ${formatDate(filtro.fechaInicio)}`);
                      if (filtro.fechaFin) parts.push(`Hasta ${formatDate(filtro.fechaFin)}`);
                      if (filtro.nombreEvento) parts.push(`Evento: ${filtro.nombreEvento}`);
                      if (filtro.minParticipantes != null && filtro.minParticipantes !== "") parts.push(`Min ${filtro.minParticipantes}`);
                      if (filtro.maxParticipantes != null && filtro.maxParticipantes !== "") parts.push(`Max ${filtro.maxParticipantes}`);

                      if (parts.length === 0) {
                        return <div style={{ fontStyle: "italic", color: '#6c757d' }}>Sin filtros específicos</div>;
                      }

                      return <div style={{ color: '#6c757d', fontSize: '0.9rem', opacity: 0.9 }}>{parts.join(' · ')}</div>;
                    })()}
            </div>

            <div className="action-buttons" style={{ justifyContent: 'center' }}>
              <button onClick={() => handleLoadSavedFilter(filtro)} className="btn btn-primary btn-sm">Cargar</button>
              <button onClick={() => handleEditFilter(filtro)} className="btn btn-secondary btn-sm">Editar</button>
              <button onClick={() => handleDeleteFilter(filtro.id)} className="btn btn-danger btn-sm">Eliminar</button>
            </div>
          </div>
        ))}
      </div>

      {filtros.length === 0 && (
        <div style={{
          textAlign: "center",
          padding: "40px",
          backgroundColor: "#f8f9fa",
          borderRadius: "8px",
          border: "2px dashed #dee2e6",
          marginTop: "20px"
        }}>
          <h4 style={{ color: "#6c757d", marginBottom: "10px" }}>📭 No hay filtros guardados</h4>
          <p style={{ color: "#6c757d", margin: "0" }}>
            Crea tu primer filtro personalizado haciendo clic en "Nuevo Filtro"
          </p>
        </div>
      )}

      {/* Filtros dinámicos y resultados */}
      <div style={{ marginTop: "30px", marginBottom: "30px" }}>
        <h3>🔍 Filtros Dinámicos</h3>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: "15px", marginBottom: "20px" }}>
          <div>
            <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>
              Fecha inicio:
            </label>
            <input
              type="date"
              value={dynamicFilters.fechaInicio}
              onChange={(e) => setDynamicFilters(prev => ({ ...prev, fechaInicio: e.target.value }))}
              style={{
                width: "100%",
                padding: "8px",
                border: "1px solid #ccc",
                borderRadius: "4px",
                fontSize: "14px",
              }}
            />
          </div>

          <div>
            <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>
              Fecha fin:
            </label>
            <input
              type="date"
              value={dynamicFilters.fechaFin}
              onChange={(e) => setDynamicFilters(prev => ({ ...prev, fechaFin: e.target.value }))}
              style={{
                width: "100%",
                padding: "8px",
                border: "1px solid #ccc",
                borderRadius: "4px",
                fontSize: "14px",
              }}
            />
          </div>



          <div>
            <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>
              Mín participantes:
            </label>
            <input
              type="number"
              min="0"
              value={dynamicFilters.minParticipantes}
              onChange={(e) => setDynamicFilters(prev => ({ ...prev, minParticipantes: e.target.value }))}
              placeholder="0"
              style={{
                width: "100%",
                padding: "8px",
                border: "1px solid #ccc",
                borderRadius: "4px",
                fontSize: "14px",
              }}
            />
          </div>

          <div>
            <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>
              Máx participantes:
            </label>
            <input
              type="number"
              min="0"
              value={dynamicFilters.maxParticipantes}
              onChange={(e) => setDynamicFilters(prev => ({ ...prev, maxParticipantes: e.target.value }))}
              placeholder="100"
              style={{
                width: "100%",
                padding: "8px",
                border: "1px solid #ccc",
                borderRadius: "4px",
                fontSize: "14px",
              }}
            />
          </div>
        </div>

        <div style={{ display: "flex", gap: "10px", marginBottom: "20px" }}>
          <button onClick={applyDynamicFilters} disabled={loadingResults} className="btn btn-primary">
            {loadingResults ? "🔄 Aplicando..." : "🔍 Aplicar Filtros"}
          </button>
          <button onClick={() => setDynamicFilters({ fechaInicio: "", fechaFin: "", nombreEvento: "", minParticipantes: "", maxParticipantes: "" })} className="btn btn-secondary">
            🧹 Limpiar
          </button>
        </div>
      </div>

      {/* Estadísticas */}
      {filteredEvents.length > 0 && (
        <div style={{ marginBottom: "30px" }}>
          <h3>📊 Estadísticas</h3>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(150px, 1fr))", gap: "15px" }}>
            <div style={{
              backgroundColor: "#e7f3ff",
              padding: "15px",
              borderRadius: "8px",
              textAlign: "center",
              border: "1px solid #b3d9ff"
            }}>
              <h4 style={{ margin: "0 0 5px 0", color: "#0056b3" }}>{calculateStats().totalEvents}</h4>
              <p style={{ margin: 0, color: "#0056b3", fontSize: "0.9rem" }}>Total Eventos</p>
            </div>
            <div style={{
              backgroundColor: "#f0f9ff",
              padding: "15px",
              borderRadius: "8px",
              textAlign: "center",
              border: "1px solid #b3e5fc"
            }}>
              <h4 style={{ margin: "0 0 5px 0", color: "#0277bd" }}>{calculateStats().totalParticipants}</h4>
              <p style={{ margin: 0, color: "#0277bd", fontSize: "0.9rem" }}>Total Participantes</p>
            </div>
            <div style={{
              backgroundColor: "#fff3e0",
              padding: "15px",
              borderRadius: "8px",
              textAlign: "center",
              border: "1px solid #ffcc02"
            }}>
              <h4 style={{ margin: "0 0 5px 0", color: "#f57c00" }}>{calculateStats().avgParticipants}</h4>
              <p style={{ margin: 0, color: "#f57c00", fontSize: "0.9rem" }}>Promedio Participantes</p>
            </div>
          </div>
        </div>
      )}

      {/* Reporte agrupado */}
      {filteredEvents.length > 0 && (
        <div style={{ marginBottom: "30px" }}>
          <h3>📈 Reporte Agrupado por Participantes</h3>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: "15px" }}>
            {Object.entries(createGroupedReport()).map(([range, data]) => (
              <div key={range} style={{
                backgroundColor: "#f8f9fa",
                padding: "15px",
                borderRadius: "8px",
                border: "1px solid #dee2e6"
              }}>
                <h4 style={{ margin: "0 0 10px 0", color: "#495057" }}>{range} participantes</h4>
                <p style={{ margin: "0 0 5px 0", color: "#6c757d" }}>Eventos: {data.count}</p>
                <p style={{ margin: 0, color: "#6c757d" }}>Total participantes: {data.totalParticipants}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Tabla de resultados */}
      {filteredEvents.length > 0 && (
        <div style={{ marginBottom: "30px" }}>
          <h3>📋 Eventos Filtrados</h3>
          <div style={{ overflowX: "auto" }}>
            <table style={{
              width: "100%",
              borderCollapse: "collapse",
              backgroundColor: "white",
              boxShadow: "0 2px 4px rgba(0,0,0,0.1)"
            }}>
              <thead>
                <tr style={{ backgroundColor: "#f8f9fa" }}>
                  <th style={{ padding: "12px", textAlign: "left", border: "1px solid #dee2e6", fontWeight: "bold" }}>Nombre Evento</th>
                  <th style={{ padding: "12px", textAlign: "left", border: "1px solid #dee2e6", fontWeight: "bold" }}>Fecha</th>
                  <th style={{ padding: "12px", textAlign: "center", border: "1px solid #dee2e6", fontWeight: "bold" }}>Participantes</th>
                  <th style={{ padding: "12px", textAlign: "left", border: "1px solid #dee2e6", fontWeight: "bold" }}>Lista Participantes</th>
                </tr>
              </thead>
              <tbody>
                {filteredEvents.map((event, index) => (
                  <tr key={index} style={{ borderBottom: "1px solid #dee2e6" }}>
                    <td style={{ padding: "12px", border: "1px solid #dee2e6" }}>{event.nombreEvento}</td>
                    <td style={{ padding: "12px", border: "1px solid #dee2e6" }}>
                      {new Date(event.fechaEvento).toLocaleDateString()}
                    </td>
                    <td style={{ padding: "12px", textAlign: "center", border: "1px solid #dee2e6" }}>
                      {event.numeroParticipantes}
                    </td>
                    <td style={{ padding: "12px", border: "1px solid #dee2e6" }}>
                      {event.participantes.join(", ")}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Modal de formulario */}
      {showForm && (
        <div style={{
          position: "fixed",
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: "rgba(0,0,0,0.5)",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          zIndex: 1000,
        }}>
          <div style={{
            backgroundColor: "white",
            padding: "20px",
            borderRadius: "8px",
            width: "90%",
            maxWidth: "500px",
            maxHeight: "90vh",
            overflowY: "auto",
          }}>
            <h3 style={{ marginTop: 0 }}>
              {editingFilter ? "✏️ Editar Filtro" : "➕ Nuevo Filtro"}
            </h3>

            <div style={{ display: "flex", flexDirection: "column", gap: "15px", marginTop: "20px" }}>
              <div>
                <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>
                  Nombre del filtro: <span style={{ color: "red" }}>*</span>
                </label>
                <input
                  type="text"
                  value={filterForm.nombre}
                  onChange={(e) => setFilterForm(prev => ({ ...prev, nombre: e.target.value }))}
                  placeholder="Ej: Eventos del mes pasado"
                  style={{
                    width: "100%",
                    padding: "8px",
                    border: "1px solid #ccc",
                    borderRadius: "4px",
                    fontSize: "14px",
                  }}
                />
              </div>

              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "15px" }}>
                <div>
                  <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>
                    Fecha inicio:
                  </label>
                  <input
                    type="date"
                    value={filterForm.fechaInicio}
                    onChange={(e) => setFilterForm(prev => ({ ...prev, fechaInicio: e.target.value }))}
                    style={{
                      width: "100%",
                      padding: "8px",
                      border: "1px solid #ccc",
                      borderRadius: "4px",
                      fontSize: "14px",
                    }}
                  />
                </div>

                <div>
                  <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>
                    Fecha fin:
                  </label>
                  <input
                    type="date"
                    value={filterForm.fechaFin}
                    onChange={(e) => setFilterForm(prev => ({ ...prev, fechaFin: e.target.value }))}
                    style={{
                      width: "100%",
                      padding: "8px",
                      border: "1px solid #ccc",
                      borderRadius: "4px",
                      fontSize: "14px",
                    }}
                  />
                </div>
              </div>



              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "15px" }}>
                <div>
                  <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>
                    Mínimo participantes:
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={filterForm.minParticipantes}
                    onChange={(e) => setFilterForm(prev => ({ ...prev, minParticipantes: e.target.value }))}
                    placeholder="0"
                    style={{
                      width: "100%",
                      padding: "8px",
                      border: "1px solid #ccc",
                      borderRadius: "4px",
                      fontSize: "14px",
                    }}
                  />
                </div>

                <div>
                  <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>
                    Máximo participantes:
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={filterForm.maxParticipantes}
                    onChange={(e) => setFilterForm(prev => ({ ...prev, maxParticipantes: e.target.value }))}
                    placeholder="100"
                    style={{
                      width: "100%",
                      padding: "8px",
                      border: "1px solid #ccc",
                      borderRadius: "4px",
                      fontSize: "14px",
                    }}
                  />
                </div>
              </div>
            </div>

            <div style={{ display: "flex", gap: "10px", marginTop: "20px", justifyContent: "flex-end" }}>
              <button onClick={handleCancel} className="btn btn-secondary">Cancelar</button>
              <button onClick={handleSaveFilter} className="btn btn-primary">{editingFilter ? "💾 Guardar Cambios" : "💾 Guardar Filtro"}</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default EventFilterManager;
