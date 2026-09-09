import type { NextConfig } from "next";
import { getBackendOrigin } from "./lib/api/config";

const nextConfig: NextConfig = {
  async rewrites() {
    const backend = getBackendOrigin();
    return [
      {
        source: "/api/:path*",
        destination: `${backend}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
