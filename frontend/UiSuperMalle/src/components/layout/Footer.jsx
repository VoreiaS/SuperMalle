import { ChefHat } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="relative bg-[#0a0a0a] text-text-secondary mt-auto border-t border-copper-500/20 overflow-hidden">
      {/* Noise overlay */}
      <div className="absolute inset-0 bg-noise pointer-events-none" />

      <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-14">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-10">
          {/* Brand */}
          <div className="col-span-1 md:col-span-2">
            <div className="flex items-center gap-3 mb-4">
              <div
                className="w-9 h-9 rounded-lg flex items-center justify-center"
                style={{ background: 'linear-gradient(135deg, #c8663e, #a84d28)' }}
              >
                <ChefHat className="w-5 h-5 text-white" />
              </div>
              <span className="font-body font-bold text-lg text-text-primary">SuperMalle</span>
            </div>
            <p className="text-sm leading-relaxed max-w-sm text-text-dim">
              Handcrafted food, delivered fresh. Every dish is made with the finest ingredients
              and a commitment to exceptional flavor.
            </p>
          </div>

          {/* Quick Links */}
          <div>
            <h4 className="font-body font-bold text-xs text-text-primary uppercase tracking-widest mb-4">
              Quick Links
            </h4>
            <ul className="space-y-3 text-sm">
              <li>
                <Link to="/menu" className="hover:text-copper-500 transition-colors">Menu</Link>
              </li>
              <li>
                <Link to="/orders" className="hover:text-copper-500 transition-colors">Track Order</Link>
              </li>
              <li>
                <Link to="/cart" className="hover:text-copper-500 transition-colors">Cart</Link>
              </li>
            </ul>
          </div>

          {/* Contact */}
          <div>
            <h4 className="font-body font-bold text-xs text-text-primary uppercase tracking-widest mb-4">
              Contact
            </h4>
            <ul className="space-y-3 text-sm text-text-dim">
              <li>123 Food Street</li>
              <li>New York, NY 10001</li>
              <li className="text-copper-500">(555) 123-4567</li>
            </ul>
          </div>
        </div>

        <div className="border-t border-white/[0.06] mt-10 pt-8 text-sm text-text-dim text-center flex items-center justify-center gap-2">
          &copy; {new Date().getFullYear()} SuperMalle
          <span className="text-copper-500">&bull;</span>
          All rights reserved.
        </div>
      </div>
    </footer>
  );
}
