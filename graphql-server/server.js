const express = require('express');
const { ApolloServer, gql } = require('apollo-server-express');
const mysql = require('mysql2/promise');
const cors = require('cors');

// Configuración de base de datos
const dbConfig = {
  host: process.env.DB_HOST || 'localhost',
  port: process.env.DB_PORT || 3306,
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || 'loquesea2008',
  database: process.env.DB_NAME || 'tp-distribuidos'
};

// Pool de conexiones
const pool = mysql.createPool(dbConfig);

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

  type DonationReport {
    stats: DonationStats!
    donations: [Donation!]!
  }

  type Query {
    donationsStats(userId: Int!): DonationStats!
    donationsReport(userId: Int!, filters: DonationFilters): DonationReport!
  }
`;

// Resolvers
const resolvers = {
  Query: {
    donationsStats: async (_, { userId }) => {
      try {
        // Verificar permisos del usuario
        const [userRows] = await pool.execute(
          'SELECT rol FROM usuario WHERE id = ? AND activo = true',
          [userId]
        );
        
        if (!userRows.length || !['PRESIDENTE', 'VOCAL'].includes(userRows[0].rol)) {
          throw new Error('Sin permisos para acceder a estadísticas');
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
          totalCantidad: stats[0].totalCantidad || 0
        };
      } catch (error) {
        throw new Error(`Error al obtener estadísticas: ${error.message}`);
      }
    },

    donationsReport: async (_, { userId, filters = {} }) => {
      try {
        // Verificar permisos del usuario
        const [userRows] = await pool.execute(
          'SELECT rol FROM usuario WHERE id = ? AND activo = true',
          [userId]
        );
        
        if (!userRows.length || !['PRESIDENTE', 'VOCAL'].includes(userRows[0].rol)) {
          throw new Error('Sin permisos para acceder al reporte');
        }

        // Construir query con filtros
        let query = 'SELECT * FROM inventario_de_donaciones WHERE 1=1';
        const params = [];

        if (filters.categoria) {
          query += ' AND categoria = ?';
          params.push(filters.categoria);
        }

        if (filters.fechaDesde) {
          query += ' AND fecha_alta >= ?';
          params.push(filters.fechaDesde);
        }

        if (filters.fechaHasta) {
          query += ' AND fecha_alta <= ?';
          params.push(filters.fechaHasta);
        }

        if (typeof filters.eliminado === 'boolean') {
          query += ' AND eliminado = ?';
          params.push(filters.eliminado ? 1 : 0);
        }

        if (filters.cantidadMin) {
          query += ' AND cantidad >= ?';
          params.push(filters.cantidadMin);
        }

        if (filters.cantidadMax) {
          query += ' AND cantidad <= ?';
          params.push(filters.cantidadMax);
        }

        query += ' ORDER BY fecha_alta DESC';

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
          statsQuery += ' AND categoria = ?';
        }
        if (filters.fechaDesde) {
          statsQuery += ' AND fecha_alta >= ?';
        }
        if (filters.fechaHasta) {
          statsQuery += ' AND fecha_alta <= ?';
        }
        if (typeof filters.eliminado === 'boolean') {
          statsQuery += ' AND eliminado = ?';
        }
        if (filters.cantidadMin) {
          statsQuery += ' AND cantidad >= ?';
        }
        if (filters.cantidadMax) {
          statsQuery += ' AND cantidad <= ?';
        }

        const [stats] = await pool.execute(statsQuery, statsParams);

        return {
          stats: {
            total: stats[0].total || 0,
            activas: stats[0].activas || 0,
            eliminadas: stats[0].eliminadas || 0,
            totalCantidad: stats[0].totalCantidad || 0
          },
          donations: donations.map(donation => ({
            id: donation.id,
            categoria: donation.categoria,
            descripcion: donation.descripcion,
            cantidad: donation.cantidad,
            fechaAlta: donation.fecha_alta,
            eliminado: donation.eliminado === 1,
            usuarioAlta: donation.usuario_alta
          }))
        };
      } catch (error) {
        throw new Error(`Error al generar reporte: ${error.message}`);
      }
    }
  }
};

async function startServer() {
  const app = express();
  app.use(cors());

  const server = new ApolloServer({
    typeDefs,
    resolvers,
    context: ({ req }) => {
      return {
        userId: req.headers.userid
      };
    }
  });

  await server.start();
  server.applyMiddleware({ app });

  const PORT = process.env.PORT || 4000;
  app.listen(PORT, () => {
    console.log(`🚀 GraphQL Server running at http://localhost:${PORT}${server.graphqlPath}`);
    console.log(`🎮 GraphQL Playground: http://localhost:${PORT}${server.graphqlPath}`);
  });
}

startServer().catch(error => {
  console.error('Error starting server:', error);
});