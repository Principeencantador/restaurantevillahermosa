const navToggle =document.querySelector(".nav-toggle");
const navMenu =document.querySelector(".nav-menu");

navToggle.addEventListener("click", () => {
 navMenu.classList.toggle("nav-menu_visible")
 document.addEventListener('DOMContentLoaded', () => {
  const btn = document.getElementById('nav-toggle');
  if (!btn) return;               // sale si no existe en esta página
  btn.addEventListener('click', () => {
    // ... tu toggle de menú ...
  });
});
});
