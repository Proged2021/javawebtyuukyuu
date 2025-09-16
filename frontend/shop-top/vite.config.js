import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  base: "/javawebtyuukyuu/shop-top/",
  build: {
    outDir: "../../webapp/shop-top",
    emptyOutDir: true
  }
});
