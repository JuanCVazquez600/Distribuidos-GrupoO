import React, { useState } from 'react';
import axios from 'axios';

const Login = ({ onLoginSuccess }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const response = await axios.get('http://localhost:5000/login', {
        params: {
          usuarioEmail: email,
          clave: password
        }
      });

      if (response.data && response.data.exito) {
        onLoginSuccess(response.data.usuario);
      } else {
        setError(response.data.mensaje || 'Credenciales inválidas');
      }
    } catch (err) {
      setError('Error al iniciar sesión. Verifica tu conexión.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: 'var(--spacing-lg)',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }}>
      <div style={{
        width: '100%',
        maxWidth: '420px',
        animation: 'slideIn 0.5s ease-out'
      }}>
        {/* Logo/Brand Section */}
        <div style={{
          textAlign: 'center',
          marginBottom: 'var(--spacing-2xl)',
          color: 'white'
        }}>
          <h1 style={{
            fontSize: '2.5rem',
            fontWeight: '700',
            margin: '0 0 var(--spacing-sm)',
            textShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
            letterSpacing: '-0.025em'
          }}>
            Bienvenido
          </h1>
          <p style={{
            fontSize: '1.1rem',
            opacity: '0.9',
            margin: 0,
            fontWeight: '400',
            color: 'rgba(34, 31, 31, 0.85)'
          }}>
            Sistema de Gestión Solidaria
          </p>
        </div>

        {/* Login Card */}
        <div style={{
          background: 'var(--bg-primary)',
          borderRadius: 'var(--border-radius-xl)',
          padding: 'var(--spacing-2xl)',
          boxShadow: 'var(--shadow-2xl)',
          border: '1px solid rgba(255, 255, 255, 0.2)',
          backdropFilter: 'blur(20px)',
          position: 'relative',
          overflow: 'hidden'
        }}>
          {/* Decorative background elements */}
          <div style={{
            position: 'absolute',
            top: '-50%',
            right: '-50%',
            width: '200%',
            height: '200%',
            background: 'radial-gradient(circle, rgba(99, 102, 241, 0.05) 0%, transparent 70%)',
            pointerEvents: 'none'
          }}></div>

          <div style={{
            position: 'absolute',
            bottom: '-30%',
            left: '-30%',
            width: '160%',
            height: '160%',
            background: 'radial-gradient(circle, rgba(6, 182, 212, 0.03) 0%, transparent 70%)',
            pointerEvents: 'none'
          }}></div>

          <div style={{ position: 'relative', zIndex: 1 }}>
            <div style={{ textAlign: 'center', marginBottom: 'var(--spacing-2xl)' }}>
              <h2 style={{
                fontSize: '1.75rem',
                fontWeight: '600',
                color: 'var(--text-primary)',
                margin: '0 0 var(--spacing-sm)'
              }}>
                Iniciar Sesión
              </h2>
              <p style={{
                color: 'var(--text-secondary)',
                margin: 0,
                fontSize: '0.95rem'
              }}>
                Ingresa tus credenciales para continuar
              </p>
            </div>

            <form onSubmit={handleSubmit} style={{ width: '100%' }}>
              <div className="form-group">
                <label className="form-label" style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 'var(--spacing-sm)',
                  fontWeight: '500'
                }}>
                  <span>📧</span>
                  Correo Electrónico
                </label>
                <input
                  type="email"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  required
                  className="form-input"
                  placeholder="tu@email.com"
                  style={{
                    fontSize: '1rem',
                    padding: 'var(--spacing-lg)',
                    border: '2px solid var(--border-color)',
                    transition: 'var(--transition-normal)'
                  }}
                  disabled={isLoading}
                />
              </div>

              <div className="form-group">
                <label className="form-label" style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 'var(--spacing-sm)',
                  fontWeight: '500'
                }}>
                  <span>🔒</span>
                  Contraseña
                </label>
                <input
                  type="password"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  required
                  className="form-input"
                  placeholder="••••••••"
                  style={{
                    fontSize: '1rem',
                    padding: 'var(--spacing-lg)',
                    border: '2px solid var(--border-color)',
                    transition: 'var(--transition-normal)'
                  }}
                  disabled={isLoading}
                />
              </div>

              {error && (
                <div className="alert alert-error" style={{
                  margin: 'var(--spacing-lg) 0',
                  animation: 'slideUp 0.3s ease-out'
                }}>
                  <span>⚠️</span>
                  <span>{error}</span>
                </div>
              )}

              <button
                type="submit"
                className="btn btn-primary btn-lg"
                style={{
                  width: '100%',
                  marginTop: 'var(--spacing-lg)',
                  padding: 'var(--spacing-lg)',
                  fontSize: '1.1rem',
                  fontWeight: '600',
                  position: 'relative',
                  overflow: 'hidden'
                }}
                disabled={isLoading}
              >
                {isLoading ? (
                  <>
                    <div className="spinner"></div>
                    Iniciando sesión...
                  </>
                ) : (
                  <>
                    <span>🚀</span>
                    Ingresar al Sistema
                  </>
                )}
              </button>
            </form>

            {/* Footer */}
            <div style={{
              textAlign: 'center',
              marginTop: 'var(--spacing-xl)',
              paddingTop: 'var(--spacing-lg)',
              borderTop: '1px solid var(--border-light)',
              color: 'var(--text-muted)',
              fontSize: '0.875rem'
            }}>
              <p style={{ margin: 0 }}>
                Sistema de Gestión para Organizaciones Solidarias
              </p>
              <p style={{
                margin: 'var(--spacing-xs) 0 0 0',
                fontSize: '0.8rem',
                opacity: 0.7
              }}>
                Versión 1.0.0
              </p>
            </div>
          </div>
        </div>

        {/* Additional Info */}
        <div style={{
          textAlign: 'center',
          marginTop: 'var(--spacing-xl)',
          color: 'rgba(255, 255, 255, 0.8)',
          fontSize: '0.9rem'
        }}>
          <p style={{ margin: 0, color: 'rgba(255, 255, 255, 0.85)' }}>
            💡 Gestiona usuarios, eventos y donaciones de manera eficiente
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
