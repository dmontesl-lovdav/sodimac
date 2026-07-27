import PropTypes from 'prop-types';
import './Layout.css';

export const Layout = ({ children }) => {
  return (
    <div className="aclaraciones-layout">
      {children}
    </div>
  );
};

Layout.propTypes = {
  children: PropTypes.node,
};
