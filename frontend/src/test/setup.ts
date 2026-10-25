import '@testing-library/jest-dom/vitest';

// antd's responsive observer uses matchMedia, which jsdom lacks. The stub evaluates
// min-width / max-width queries against window.innerWidth so Grid.useBreakpoint works in tests.
if (!window.matchMedia) {
  window.matchMedia = (query: string) => {
    const min = /min-width:\s*(\d+)px/.exec(query);
    const max = /max-width:\s*(\d+)px/.exec(query);
    const width = window.innerWidth;
    const matches = (!min || width >= Number(min[1])) && (!max || width <= Number(max[1]));
    return {
      matches,
      media: query,
      onchange: null,
      addListener: () => {},
      removeListener: () => {},
      addEventListener: () => {},
      removeEventListener: () => {},
      dispatchEvent: () => false,
    } as MediaQueryList;
  };
}

// antd Table / Segmented measure themselves with ResizeObserver
if (!('ResizeObserver' in window)) {
  class RO {
    observe() {}
    unobserve() {}
    disconnect() {}
  }
  (window as unknown as { ResizeObserver: typeof RO }).ResizeObserver = RO;
}
if (!window.HTMLElement.prototype.scrollIntoView) {
  window.HTMLElement.prototype.scrollIntoView = () => {};
}
if (!window.HTMLElement.prototype.scrollBy) {
  window.HTMLElement.prototype.scrollBy = () => {};
}
