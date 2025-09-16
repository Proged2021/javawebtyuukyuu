import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  base: "/javawebtyuukyuu/shop-top-react/",
  build: {
    outDir: "../../webapp/shop-top-react",
    emptyOutDir: true
  }
});
