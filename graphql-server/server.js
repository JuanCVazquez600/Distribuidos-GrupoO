const express = require("express");
const { ApolloServer, gql } = require("apollo-server-express");
const mysql = require("mysql2/promise");
const cors = require("cors");

// Configuración de base de datos
const dbConfig = {
  host: process.env.DB_HOST || "localhost",
  port: process.env.DB_PORT || 3306,
  user: process.env.DB_USER || "root",
  password: process.env.DB_PASSWORD || "root",
  database: process.env.DB_NAME || "distribuidos-tp",
};

// Pool de conexiones
const pool = mysql.createPool(dbConfig);

// Función auxiliar para obtener el rol del usuario
const getUserRole = async (userId) => {
  const [rows] = await pool.execute('SELECT rol FROM usuario WHERE id = ?', [userId]);
  return rows[0]?.rol;
};

// Schema GraphQL
const typeDefs = gql`
  enum CategoriaEnum {
    ROPA
    ALIMENTOS
    JUGUETES
    UTILES_ESCOLARES
  }

  input DonationFilters {
    categoria: CategoriaEnum
    fechaDesde: String
    fechaHasta: String
    eliminado: Boolean
    cantidadMin: Int
    cantidadMax: Int
  }

  # Tipo de salida para filtros (diferente del input)
  type DonationFiltersOutput {
    categoria: String
    fechaDesde: String
    fechaHasta: String
    eliminado: Boolean
    cantidadMin: Int
    cantidadMax: Int
  }

  type DonationStats {
    total: Int!
    activas: Int!
    eliminadas: Int!
    totalCantidad: Int!
  }

  type Donation {
    id: Int!
    categoria: CategoriaEnum!
    descripcion: String!
    cantidad: Int!
    fechaAlta: String!
    eliminado: Boolean!
    usuarioAlta: Int
  }

  type DonationGrouped {
    categoria: CategoriaEnum!
    eliminado: Boolean!
    totalCantidad: Int!
    conteoRegistros: Int!
  }

  type DonationReport {
    stats: DonationStats!
    donations: [Donation!]!
  }

  type DonationGroupedReport {
    stats: DonationStats!
    groupedDonations: [DonationGrouped!]!
  }

  # Tipos para filtros guardados
  type SavedFilter {
    id: Int!
    nombre: String!
    filtros: DonationFiltersOutput!
    fechaCreacion: String!
  }

  input SavedFilterInput {
    nombre: String!
    filtros: DonationFilters!
  }

  # Tipos para informe de participación en eventos
  input EventParticipationFilters {
    fechaDesde: String
    fechaHasta: String
    usuarioId: Int!
    repartoDonaciones: Boolean
  }

  type EventParticipation {
    dia: String!
    nombreEvento: String!
    descripcion: String!
    donaciones: String
  }

  type MonthlyEventParticipation {
    mes: String!
    eventos: [EventParticipation!]!
  }

  type EventParticipationReport {
    participaciones: [MonthlyEventParticipation!]!
  }

  type User {
    id: Int!
    nombre: String!
    apellido: String!
    rol: String!
  }

  type Query {
    donationsStats(userId: Int!): DonationStats!
    donationsReport(userId: Int!, filters: DonationFilters): DonationReport!
    donationsGroupedReport(
      userId: Int!
      filters: DonationFilters
    ): DonationGroupedReport!
    availableCategories(userId: Int!): [String!]!
    savedFilters(userId: Int!): [SavedFilter!]!
    eventParticipationReport(
      userId: Int!
      filters: EventParticipationFilters!
    ): EventParticipationReport!
    users: [User!]!
  }

  type Mutation {
    saveFilter(userId: Int!, input: SavedFilterInput!): SavedFilter!
    deleteSavedFilter(userId: Int!, filterId: Int!): Boolean!
    updateSavedFilterName(
      userId: Int!
      filterId: Int!
      newName: String!
    ): SavedFilter!
  }
`;

// Resolvers
const resolvers = {
  Query: {
    donationsStats: async (_, { userId }, context) => {
      try {
        // Verificar autenticación
        const contextUserId = context.userId;
        if (!contextUserId) {
          throw new Error("Usuario no autenticado");
        }

        const authenticatedUserId = parseInt(contextUserId);
        if (authenticatedUserId !== userId) {
          throw new Error("Usuario no autorizado");
        }

        // Verificar permisos del usuario
        const [userRows] = await pool.execute(
          "SELECT rol FROM usuario WHERE id = ? AND activo = true",
          [authenticatedUserId]
        );

        if (
          !userRows.length ||
          !["PRESIDENTE", "VOCAL"].includes(userRows[0].rol)
        ) {
          throw new Error("Sin permisos para acceder a estadísticas");
        }

        // Obtener estadísticas
        const [stats] = await pool.execute(`
          SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN eliminado = 0 THEN 1 ELSE 0 END) as activas,
            SUM(CASE WHEN eliminado = 1 THEN 1 ELSE 0 END) as eliminadas,
            SUM(cantidad) as totalCantidad
          FROM inventario_de_donaciones
        `);

        return {
          total: stats[0].total || 0,
          activas: stats[0].activas || 0,
          eliminadas: stats[0].eliminadas || 0,
          totalCantidad: stats[0].totalCantidad || 0,
        };
      } catch (error) {
        throw new Error(`Error al obtener estadísticas: ${error.message}`);
      }
    },

    donationsReport: async (_, { userId, filters = {} }, context) => {
      try {
        // Verificar autenticación
        const contextUserId = context.userId;
        if (!contextUserId) {
          throw new Error("Usuario no autenticado");
        }

        const authenticatedUserId = parseInt(contextUserId);
        if (authenticatedUserId !== userId) {
          throw new Error("Usuario no autorizado");
        }

        // Verificar permisos del usuario
        const [userRows] = await pool.execute(
          "SELECT rol FROM usuario WHERE id = ? AND activo = true",
          [authenticatedUserId]
        );

        if (
          !userRows.length ||
          !["PRESIDENTE", "VOCAL"].includes(userRows[0].rol)
        ) {
          throw new Error("Sin permisos para acceder al reporte");
        }

        // Construir query con filtros
        let query = "SELECT * FROM inventario_de_donaciones WHERE 1=1";
        const params = [];

        if (filters.categoria) {
          query += " AND categoria = ?";
          params.push(filters.categoria);
        }

        if (filters.fechaDesde) {
          query += " AND fecha_alta >= ?";
          params.push(filters.fechaDesde);
        }

        if (filters.fechaHasta) {
          query += " AND fecha_alta <= ?";
          params.push(filters.fechaHasta);
        }

        if (typeof filters.eliminado === "boolean") {
          query += " AND eliminado = ?";
          params.push(filters.eliminado ? 1 : 0);
        }

        if (filters.cantidadMin) {
          query += " AND cantidad >= ?";
          params.push(filters.cantidadMin);
        }

        if (filters.cantidadMax) {
          query += " AND cantidad <= ?";
          params.push(filters.cantidadMax);
        }

        query += " ORDER BY fecha_alta DESC";

        // Ejecutar consulta de donaciones
        const [donations] = await pool.execute(query, params);

        // Obtener estadísticas para el reporte filtrado
        let statsQuery = `
          SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN eliminado = 0 THEN 1 ELSE 0 END) as activas,
            SUM(CASE WHEN eliminado = 1 THEN 1 ELSE 0 END) as eliminadas,
            SUM(cantidad) as totalCantidad
          FROM inventario_de_donaciones WHERE 1=1
        `;

        // Aplicar los mismos filtros a las estadísticas
        const statsParams = [...params];
        if (filters.categoria) {
          statsQuery += " AND categoria = ?";
        }
        if (filters.fechaDesde) {
          statsQuery += " AND fecha_alta >= ?";
        }
        if (filters.fechaHasta) {
          statsQuery += " AND fecha_alta <= ?";
        }
        if (typeof filters.eliminado === "boolean") {
          statsQuery += " AND eliminado = ?";
        }
        if (filters.cantidadMin) {
          statsQuery += " AND cantidad >= ?";
        }
        if (filters.cantidadMax) {
          statsQuery += " AND cantidad <= ?";
        }

        const [stats] = await pool.execute(statsQuery, statsParams);

        return {
          stats: {
            total: stats[0].total || 0,
            activas: stats[0].activas || 0,
            eliminadas: stats[0].eliminadas || 0,
            totalCantidad: stats[0].totalCantidad || 0,
          },
          donations: donations.map((donation) => ({
            id: donation.id,
            categoria: donation.categoria,
            descripcion: donation.descripcion,
            cantidad: donation.cantidad,
            fechaAlta: donation.fecha_alta,
            eliminado: donation.eliminado === 1,
            usuarioAlta: donation.usuario_alta,
          })),
        };
      } catch (error) {
        throw new Error(`Error al generar reporte: ${error.message}`);
      }
    },

    donationsGroupedReport: async (_, { userId, filters = {} }, context) => {
      try {
        // Verificar autenticación
        const contextUserId = context.userId;
        if (!contextUserId) {
          throw new Error("Usuario no autenticado");
        }

        const authenticatedUserId = parseInt(contextUserId);
        if (authenticatedUserId !== userId) {
          throw new Error("Usuario no autorizado");
        }

        // Verificar permisos del usuario
        const [userRows] = await pool.execute(
          "SELECT rol FROM usuario WHERE id = ? AND activo = true",
          [authenticatedUserId]
        );

        if (
          !userRows.length ||
          !["PRESIDENTE", "VOCAL"].includes(userRows[0].rol)
        ) {
          throw new Error("Sin permisos para acceder al reporte agrupado");
        }

        // Construir query agrupada con filtros
        let groupedQuery = `
          SELECT 
            categoria,
            eliminado,
            SUM(cantidad) as totalCantidad,
            COUNT(*) as conteoRegistros
          FROM inventario_de_donaciones 
          WHERE 1=1
        `;
        const params = [];

        if (filters.categoria) {
          groupedQuery += " AND categoria = ?";
          params.push(filters.categoria);
        }

        if (filters.fechaDesde) {
          groupedQuery += " AND fecha_alta >= ?";
          params.push(filters.fechaDesde);
        }

        if (filters.fechaHasta) {
          groupedQuery += " AND fecha_alta <= ?";
          params.push(filters.fechaHasta);
        }

        if (typeof filters.eliminado === "boolean") {
          groupedQuery += " AND eliminado = ?";
          params.push(filters.eliminado ? 1 : 0);
        }

        if (filters.cantidadMin) {
          groupedQuery += " AND cantidad >= ?";
          params.push(filters.cantidadMin);
        }

        if (filters.cantidadMax) {
          groupedQuery += " AND cantidad <= ?";
          params.push(filters.cantidadMax);
        }

        groupedQuery +=
          " GROUP BY categoria, eliminado ORDER BY categoria, eliminado";

        // Ejecutar consulta agrupada
        const [groupedDonations] = await pool.execute(groupedQuery, params);

        // Obtener estadísticas globales con los mismos filtros
        let statsQuery = `
          SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN eliminado = 0 THEN 1 ELSE 0 END) as activas,
            SUM(CASE WHEN eliminado = 1 THEN 1 ELSE 0 END) as eliminadas,
            SUM(cantidad) as totalCantidad
          FROM inventario_de_donaciones WHERE 1=1
        `;

        // Aplicar los mismos filtros a las estadísticas
        const statsParams = [...params];
        if (filters.categoria) {
          statsQuery += " AND categoria = ?";
        }
        if (filters.fechaDesde) {
          statsQuery += " AND fecha_alta >= ?";
        }
        if (filters.fechaHasta) {
          statsQuery += " AND fecha_alta <= ?";
        }
        if (typeof filters.eliminado === "boolean") {
          statsQuery += " AND eliminado = ?";
        }
        if (filters.cantidadMin) {
          statsQuery += " AND cantidad >= ?";
        }
        if (filters.cantidadMax) {
          statsQuery += " AND cantidad <= ?";
        }

        const [stats] = await pool.execute(statsQuery, statsParams);

        return {
          stats: {
            total: stats[0].total || 0,
            activas: stats[0].activas || 0,
            eliminadas: stats[0].eliminadas || 0,
            totalCantidad: stats[0].totalCantidad || 0,
          },
          groupedDonations: groupedDonations.map((item) => ({
            categoria: item.categoria,
            eliminado: item.eliminado === 1,
            totalCantidad: item.totalCantidad || 0,
            conteoRegistros: item.conteoRegistros || 0,
          })),
        };
      } catch (error) {
        throw new Error(`Error al generar reporte agrupado: ${error.message}`);
      }
    },

    availableCategories: async (_, { userId }, context) => {
      try {
        // Verificar autenticación
        const contextUserId = context.userId;
        if (!contextUserId) {
          throw new Error("Usuario no autenticado");
        }

        const authenticatedUserId = parseInt(contextUserId);
        if (authenticatedUserId !== userId) {
          throw new Error("Usuario no autorizado");
        }

        // Verificar permisos del usuario
        const [userRows] = await pool.execute(
          "SELECT rol FROM usuario WHERE id = ? AND activo = true",
          [authenticatedUserId]
        );

        if (
          !userRows.length ||
          !["PRESIDENTE", "VOCAL"].includes(userRows[0].rol)
        ) {
          throw new Error("Sin permisos para acceder a categorías");
        }

        // Obtener categorías distintas de la base de datos
        const [categories] = await pool.execute(
          "SELECT DISTINCT categoria FROM inventario_de_donaciones ORDER BY categoria"
        );

        return categories.map((row) => row.categoria);
      } catch (error) {
        throw new Error(`Error al obtener categorías: ${error.message}`);
      }
    },

    savedFilters: async (_, { userId }, context) => {
      try {
        // Verificar autenticación
        const contextUserId = context.userId;
        if (!contextUserId) {
          throw new Error("Usuario no autenticado");
        }

        const authenticatedUserId = parseInt(contextUserId);
        if (authenticatedUserId !== userId) {
          throw new Error("Usuario no autorizado");
        }

        // Verificar permisos del usuario
        const [userRows] = await pool.execute(
          "SELECT rol FROM usuario WHERE id = ? AND activo = true",
          [authenticatedUserId]
        );

        if (
          !userRows.length ||
          !["PRESIDENTE", "VOCAL"].includes(userRows[0].rol)
        ) {
          throw new Error("Sin permisos para acceder a filtros guardados");
        }

        // Obtener filtros guardados del usuario
        const [savedFilters] = await pool.execute(
          "SELECT id, nombre, filtros_json, fecha_creacion FROM filtros_guardados WHERE usuario_id = ? ORDER BY fecha_creacion DESC",
          [userId]
        );

        return savedFilters.map((filter) => ({
          id: filter.id,
          nombre: filter.nombre,
          filtros: JSON.parse(filter.filtros_json),
          fechaCreacion: filter.fecha_creacion.toISOString(),
        }));
      } catch (error) {
        throw new Error(`Error al obtener filtros guardados: ${error.message}`);
      }
    },

    eventParticipationReport: async (_, { userId, filters }, context) => {
      try {
        // Verificar autenticación
        const contextUserId = context.userId;
        if (!contextUserId) {
          throw new Error("Usuario no autenticado");
        }

        const authenticatedUserId = parseInt(contextUserId);
        if (authenticatedUserId !== userId) {
          throw new Error("Usuario no autorizado");
        }

        // Verificar que el usuario existe y está activo
        const [userRows] = await pool.execute(
          "SELECT rol FROM usuario WHERE id = ? AND activo = true",
          [authenticatedUserId]
        );

        if (!userRows.length) {
          throw new Error("Usuario no encontrado o inactivo");
        }

        const userRole = userRows[0].rol;

        // Verificar permisos: todos los roles pueden acceder, pero con restricciones en usuarioId
        if (
          userRole !== "PRESIDENTE" &&
          userRole !== "COORDINADOR" &&
          filters.usuarioId !== authenticatedUserId
        ) {
          throw new Error("Solo puedes consultar tu propia participación");
        }

        // Construir query base con subquery para compatibilidad con sql_mode=only_full_group_by
        let subQuery = `
          SELECT
            e.id,
            DATE_FORMAT(e.fecha_evento, '%Y-%m') as mes,
            DATE_FORMAT(e.fecha_evento, '%Y-%m-%d') as dia,
            e.nombre_evento,
            e.descripcion_evento,
            GROUP_CONCAT(
              CONCAT(
                d.categoria, ': ', ed.cantidad_usada, ' (', d.descripcion, ')'
              ) SEPARATOR '; '
            ) as donaciones
          FROM evento_solidario e
          INNER JOIN evento_miembros em ON e.id = em.evento_id
          LEFT JOIN evento_donaciones ed ON e.id = ed.evento_id
          LEFT JOIN inventario_de_donaciones d ON ed.donacion_id = d.id
          WHERE em.usuario_id = ?
        `;

        const params = [filters.usuarioId];

        // Aplicar filtro de fechas opcional
        if (filters.fechaDesde) {
          subQuery += " AND e.fecha_evento >= ?";
          params.push(filters.fechaDesde);
        }

        if (filters.fechaHasta) {
          subQuery += " AND e.fecha_evento <= ?";
          params.push(filters.fechaHasta);
        }

        // Aplicar filtro de reparto de donaciones
        if (typeof filters.repartoDonaciones === "boolean") {
          if (filters.repartoDonaciones) {
            subQuery += " AND ed.evento_id IS NOT NULL";
          } else {
            subQuery += " AND ed.evento_id IS NULL";
          }
        }

        subQuery += " GROUP BY e.id, DATE_FORMAT(e.fecha_evento, '%Y-%m'), DATE_FORMAT(e.fecha_evento, '%Y-%m-%d'), e.nombre_evento, e.descripcion_evento";

        let query = `SELECT mes, dia, nombre_evento, descripcion_evento, donaciones FROM (${subQuery}) as sub ORDER BY mes DESC, dia DESC`;

        // Ejecutar consulta
        const [rows] = await pool.execute(query, params);

        // Si no hay filas, devolver estructura vacía en lugar de error
        if (rows.length === 0) {
          return { participaciones: [] };
        }

        // Agrupar por mes
        const participacionesPorMes = {};

        rows.forEach((row) => {
          const mes = row.mes;
          if (!participacionesPorMes[mes]) {
            participacionesPorMes[mes] = [];
          }

          participacionesPorMes[mes].push({
            dia: row.dia,
            nombreEvento: row.nombre_evento,
            descripcion: row.descripcion_evento,
            donaciones: row.donaciones || null,
          });
        });

        // Convertir a formato de respuesta
        const participaciones = Object.keys(participacionesPorMes)
          .sort((a, b) => b.localeCompare(a)) // Ordenar meses descendente
          .map((mes) => ({
            mes,
            eventos: participacionesPorMes[mes],
          }));

        return { participaciones };
      } catch (error) {
        throw new Error(
          `Error al generar reporte de participación: ${error.message}`
        );
      }
    },

    users: async (_, __, context) => {
      try {
        const userId = context.userId;

        // Verificar permisos del usuario
        const [userRows] = await pool.execute(
          "SELECT rol FROM usuario WHERE id = ? AND activo = true",
          [userId]
        );

        if (!userRows.length) {
          throw new Error("Usuario no encontrado o inactivo");
        }

        const userRole = userRows[0].rol;

        // Solo PRESIDENTE y COORDINADOR pueden ver la lista de usuarios
        if (userRole !== "PRESIDENTE" && userRole !== "COORDINADOR") {
          throw new Error("Sin permisos para acceder a la lista de usuarios");
        }

        // Obtener lista de usuarios activos
        const [users] = await pool.execute(
          "SELECT id, nombre, apellido, rol FROM usuario WHERE activo = true ORDER BY nombre, apellido"
        );

        return users.map((user) => ({
          id: user.id,
          nombre: user.nombre,
          apellido: user.apellido,
          rol: user.rol,
        }));
      } catch (error) {
        throw new Error(`Error al obtener lista de usuarios: ${error.message}`);
      }
    },
  },

  Mutation: {
    saveFilter: async (_, { userId, input }, context) => {
      try {
        // Verificar autenticación
        const contextUserId = context.userId;
        if (!contextUserId) {
          throw new Error("Usuario no autenticado");
        }

        const authenticatedUserId = parseInt(contextUserId);
        if (authenticatedUserId !== userId) {
          throw new Error("Usuario no autorizado");
        }

        // Verificar permisos del usuario
        const [userRows] = await pool.execute(
          "SELECT rol FROM usuario WHERE id = ? AND activo = true",
          [authenticatedUserId]
        );

        if (
          !userRows.length ||
          !["PRESIDENTE", "VOCAL"].includes(userRows[0].rol)
        ) {
          throw new Error("Sin permisos para guardar filtros");
        }

        // Validar que el nombre no esté vacío
        if (!input.nombre || input.nombre.trim().length === 0) {
          throw new Error("El nombre del filtro no puede estar vacío");
        }

        // Validar longitud del nombre
        if (input.nombre.length > 100) {
          throw new Error(
            "El nombre del filtro no puede exceder 100 caracteres"
          );
        }

        // Verificar que no exista un filtro con el mismo nombre para este usuario
        const [existingFilter] = await pool.execute(
          "SELECT id FROM filtros_guardados WHERE usuario_id = ? AND nombre = ?",
          [userId, input.nombre.trim()]
        );

        if (existingFilter.length > 0) {
          throw new Error("Ya existe un filtro con ese nombre");
        }

        // Insertar el nuevo filtro
        const [result] = await pool.execute(
          "INSERT INTO filtros_guardados (usuario_id, nombre, filtros_json) VALUES (?, ?, ?)",
          [userId, input.nombre.trim(), JSON.stringify(input.filtros)]
        );

        // Obtener el filtro creado
        const [newFilter] = await pool.execute(
          "SELECT id, nombre, filtros_json, fecha_creacion FROM filtros_guardados WHERE id = ?",
          [result.insertId]
        );

        return {
          id: newFilter[0].id,
          nombre: newFilter[0].nombre,
          filtros: JSON.parse(newFilter[0].filtros_json),
          fechaCreacion: newFilter[0].fecha_creacion.toISOString(),
        };
      } catch (error) {
        throw new Error(`Error al guardar filtro: ${error.message}`);
      }
    },

    deleteSavedFilter: async (_, { userId, filterId }, context) => {
      try {
        // Verificar autenticación
        const contextUserId = context.userId;
        if (!contextUserId) {
          throw new Error("Usuario no autenticado");
        }

        const authenticatedUserId = parseInt(contextUserId);
        if (authenticatedUserId !== userId) {
          throw new Error("Usuario no autorizado");
        }

        // Verificar permisos del usuario
        const [userRows] = await pool.execute(
          "SELECT rol FROM usuario WHERE id = ? AND activo = true",
          [authenticatedUserId]
        );

        if (
          !userRows.length ||
          !["PRESIDENTE", "VOCAL"].includes(userRows[0].rol)
        ) {
          throw new Error("Sin permisos para eliminar filtros");
        }

        // Verificar que el filtro existe y pertenece al usuario
        const [filterRows] = await pool.execute(
          "SELECT id FROM filtros_guardados WHERE id = ? AND usuario_id = ?",
          [filterId, userId]
        );

        if (!filterRows.length) {
          throw new Error(
            "Filtro no encontrado o no tienes permisos para eliminarlo"
          );
        }

        // Eliminar el filtro
        const [result] = await pool.execute(
          "DELETE FROM filtros_guardados WHERE id = ? AND usuario_id = ?",
          [filterId, userId]
        );

        return result.affectedRows > 0;
      } catch (error) {
        throw new Error(`Error al eliminar filtro: ${error.message}`);
      }
    },

    updateSavedFilterName: async (
      _,
      { userId, filterId, newName },
      context
    ) => {
      try {
        // Verificar autenticación
        const contextUserId = context.userId;
        if (!contextUserId) {
          throw new Error("Usuario no autenticado");
        }

        const authenticatedUserId = parseInt(contextUserId);
        if (authenticatedUserId !== userId) {
          throw new Error("Usuario no autorizado");
        }

        // Verificar permisos del usuario
        const [userRows] = await pool.execute(
          "SELECT rol FROM usuario WHERE id = ? AND activo = true",
          [authenticatedUserId]
        );

        if (
          !userRows.length ||
          !["PRESIDENTE", "VOCAL"].includes(userRows[0].rol)
        ) {
          throw new Error("Sin permisos para editar filtros");
        }

        // Validar que el nuevo nombre no esté vacío
        if (!newName || newName.trim().length === 0) {
          throw new Error("El nombre del filtro no puede estar vacío");
        }

        // Validar longitud del nombre
        if (newName.length > 100) {
          throw new Error(
            "El nombre del filtro no puede exceder 100 caracteres"
          );
        }

        // Verificar que el filtro existe y pertenece al usuario
        const [filterRows] = await pool.execute(
          "SELECT id FROM filtros_guardados WHERE id = ? AND usuario_id = ?",
          [filterId, userId]
        );

        if (!filterRows.length) {
          throw new Error(
            "Filtro no encontrado o no tienes permisos para editarlo"
          );
        }

        // Verificar que no exista otro filtro con el mismo nombre para este usuario
        const [existingFilter] = await pool.execute(
          "SELECT id FROM filtros_guardados WHERE usuario_id = ? AND nombre = ? AND id != ?",
          [userId, newName.trim(), filterId]
        );

        if (existingFilter.length > 0) {
          throw new Error("Ya existe otro filtro con ese nombre");
        }

        // Actualizar el nombre del filtro
        const [result] = await pool.execute(
          "UPDATE filtros_guardados SET nombre = ? WHERE id = ? AND usuario_id = ?",
          [newName.trim(), filterId, userId]
        );

        if (result.affectedRows === 0) {
          throw new Error("No se pudo actualizar el filtro");
        }

        // Obtener el filtro actualizado
        const [updatedFilter] = await pool.execute(
          "SELECT id, nombre, filtros_json, fecha_creacion FROM filtros_guardados WHERE id = ?",
          [filterId]
        );

        return {
          id: updatedFilter[0].id,
          nombre: updatedFilter[0].nombre,
          filtros: JSON.parse(updatedFilter[0].filtros_json),
          fechaCreacion: updatedFilter[0].fecha_creacion.toISOString(),
        };
      } catch (error) {
        throw new Error(
          `Error al actualizar nombre del filtro: ${error.message}`
        );
      }
    },
  },
};

async function startServer() {
  const app = express();
  app.use(cors());

  const server = new ApolloServer({
    typeDefs,
    resolvers,
    context: ({ req }) => {
      return {
        userId: req.headers.userid,
      };
    },
  });

  await server.start();
  server.applyMiddleware({ app });

  const PORT = process.env.PORT || 4000;
  app.listen(PORT, () => {
    console.log(
      `GraphQL Server running at http://localhost:${PORT}${server.graphqlPath}`
    );
    console.log(
      `GraphQL Playground: http://localhost:${PORT}${server.graphqlPath}`
    );
  });
}

startServer().catch((error) => {
  console.error("Error starting server:", error);
});
