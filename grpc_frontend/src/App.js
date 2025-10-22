import React, { useState } from 'react';
import { ApolloClient, InMemoryCache, ApolloProvider, createHttpLink } from '@apollo/client';
import { setContext } from '@apollo/client/link/context';
import Dashboard from './components/Dashboard';
import Login from './components/Login';
import './App.css';

// Configurar el enlace HTTP para GraphQL
const httpLink = createHttpLink({
  uri: 'http://localhost:4000/graphql', // URL de tu servidor GraphQL
});

// Configurar el enlace de autenticación
const authLink = setContext((_, { headers }) => {
  // Obtener el userId del localStorage
  const userId = localStorage.getItem('userId');
  
  return {
    headers: {
      ...headers,
      // Enviar userId en los headers si está disponible
      userid: userId || '',
      authorization: userId ? `Bearer ${userId}` : '',
    }
  }
});

// Crear el cliente Apollo
const client = new ApolloClient({
  link: authLink.concat(httpLink),
  cache: new InMemoryCache(),
  // Configuraciones opcionales
  defaultOptions: {
    watchQuery: {
      errorPolicy: 'ignore',
    },
    query: {
      errorPolicy: 'all',
    },
  }
});

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [currentUser, setCurrentUser] = useState(null);

  const handleLoginSuccess = (userData) => {
    setCurrentUser(userData);
    setIsLoggedIn(true);
  };

  const handleLogout = () => {
    localStorage.removeItem("userId");
    setCurrentUser(null);
    setIsLoggedIn(false);
  };

  return (
    <ApolloProvider client={client}>
      <div className="App">
        {isLoggedIn ? (
          <Dashboard currentUser={currentUser} onLogout={handleLogout} />
        ) : (
          <Login onLoginSuccess={handleLoginSuccess} />
        )}
      </div>
    </ApolloProvider>
  );
}

export default App;
