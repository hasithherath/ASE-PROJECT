import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, BookOpen, Settings, LogOut, Library, ChevronRight } from 'lucide-react';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import BookCatalog from './pages/BookCatalog';
import LibrarianView from './pages/LibrarianView';

const Sidebar = () => {
  const location = useLocation();
  const userRoles = JSON.parse(localStorage.getItem('roles') || '[]');
  const isLibrarian = userRoles.includes('ROLE_LIBRARIAN');

  if (location.pathname === '/login') return null;

  const NavItem = ({ to, icon: Icon, label, isAdmin }) => {
    const isActive = location.pathname === to;
    if (isAdmin && !isLibrarian) return null;

    return (
      <Link
        to={to}
        className={`flex items-center space-x-3 px-4 py-3 rounded-xl transition-all duration-200 group ${
          isActive 
            ? 'bg-blue-600 text-white shadow-lg shadow-blue-600/20' 
            : 'text-slate-600 hover:bg-slate-100 hover:text-blue-600'
        }`}
      >
        <Icon size={20} />
        <span className="font-semibold flex-1">{label}</span>
        {isActive && <ChevronRight size={16} />}
      </Link>
    );
  };

  const handleLogout = () => {
    localStorage.clear();
    window.location.href = '/login';
  };

  return (
    <div className="w-64 bg-white border-r border-slate-200 flex flex-col h-screen sticky top-0 shadow-sm">
      <div className="p-6 flex items-center space-x-3">
        <div className="p-2 bg-blue-600 rounded-lg shadow-md shadow-blue-200">
          <Library size={24} className="text-white" />
        </div>
        <span className="text-xl font-bold tracking-tight text-slate-900">LMS Pro</span>
      </div>

      <nav className="flex-1 px-4 py-4 space-y-2">
        <NavItem to="/dashboard" icon={LayoutDashboard} label="Dashboard" />
        <NavItem to="/catalog" icon={BookOpen} label="Book Catalog" />
        <NavItem to="/librarian" icon={Settings} label="Inventory" isAdmin />
      </nav>

      <div className="p-4 border-t border-slate-100">
        <button
          onClick={handleLogout}
          className="flex items-center space-x-3 px-4 py-3 rounded-xl text-slate-500 hover:bg-red-50 hover:text-red-600 w-full transition-all duration-200"
        >
          <LogOut size={20} />
          <span className="font-semibold">Logout</span>
        </button>
      </div>
    </div>
  );
};

const PrivateRoute = ({ children, role }) => {
  const token = localStorage.getItem('token');
  const userRoles = JSON.parse(localStorage.getItem('roles') || '[]');
  
  if (!token) return <Navigate to="/login" />;
  if (role && !userRoles.includes(role)) return <Navigate to="/dashboard" />;
  
  return children;
};

function App() {
  return (
    <Router>
      <div className="flex min-h-screen bg-slate-50 text-slate-900">
        <Sidebar />
        <main className="flex-1 p-8 overflow-y-auto">
          <div className="max-w-6xl mx-auto">
            <Routes>
              <Route path="/login" element={<Login />} />
              <Route path="/dashboard" element={
                <PrivateRoute>
                  <Dashboard />
                </PrivateRoute>
              } />
              <Route path="/catalog" element={
                <PrivateRoute>
                  <BookCatalog />
                </PrivateRoute>
              } />
              <Route path="/librarian" element={
                <PrivateRoute role="ROLE_LIBRARIAN">
                  <LibrarianView />
                </PrivateRoute>
              } />
              <Route path="/" element={<Navigate to="/dashboard" />} />
            </Routes>
          </div>
        </main>
      </div>
    </Router>
  );
}

export default App;
