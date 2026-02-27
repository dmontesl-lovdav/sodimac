import '@testing-library/jest-dom';

// matchMedia (muchos componentes lo usan para media queries)
if (!window.matchMedia) {
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: jest.fn().mockImplementation((query) => ({
      matches: false,
      media: query,
      onchange: null,
      addListener: jest.fn(),       // deprecated
      removeListener: jest.fn(),    // deprecated
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
      dispatchEvent: jest.fn(),
    })),
  });
}

// IntersectionObserver
if (!('IntersectionObserver' in window)) {
  class MockIntersectionObserver {
    observe() { }
    unobserve() { }
    disconnect() { }
    takeRecords() { return []; }
  }
  window.IntersectionObserver = MockIntersectionObserver;
}

// ResizeObserver
if (!('ResizeObserver' in window)) {
  class MockResizeObserver {
    observe() { }
    unobserve() { }
    disconnect() { }
  }
  window.ResizeObserver = MockResizeObserver;
}

// scrollTo stub
if (!window.scrollTo) {
  window.scrollTo = jest.fn();
}

// URL.createObjectURL
if (!global.URL.createObjectURL) {
  global.URL.createObjectURL = jest.fn(() => 'blob:mock');
}

// TextEncoder / TextDecoder
if (typeof global.TextEncoder === 'undefined') {
  const { TextEncoder, TextDecoder } = require('util');
  global.TextEncoder = TextEncoder;
  global.TextDecoder = TextDecoder;
}

// crypto.getRandomValues / randomUUID
if (!global.crypto) {
  const { webcrypto } = require('crypto');
  global.crypto = webcrypto;
}

const originalError = console.error;

console.error = (...args) => {
  const msg = args?.[0];

  if (typeof msg === 'string') {
    if (msg.includes('ReactDOMTestUtils.act is deprecated')) return;
    if (msg.includes('act(...)')) return;
  }

  originalError(...args);
};
