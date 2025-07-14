const visualizar = document.getElementById("visualizar");
const passwordInput = document.getElementById("password-input");
const visualizar2 = document.getElementById("visualizar2");
const passwordInput2 = document.getElementById("password-input2");

visualizar.addEventListener("click", () => {
    if (passwordInput.type === "password") {
        passwordInput.type = "text";
        visualizar.classList.remove("fa-eye");
        visualizar.classList.add("fa-eye-slash");
    } else {
        passwordInput.type = "password";
        visualizar.classList.remove("fa-eye-slash");
        visualizar.classList.add("fa-eye");
    }
});
visualizar2.addEventListener("click", () => {
    if (passwordInput2.type === "password") {
        passwordInput2.type = "text";
        visualizar2.classList.remove("fa-eye");
        visualizar2.classList.add("fa-eye-slash");
    } else {
        passwordInput2.type = "password";
        visualizar2.classList.remove("fa-eye-slash");
        visualizar2.classList.add("fa-eye");
    }
    document.addEventListener('DOMContentLoaded', () => {
  const eye = document.getElementById('togglePassword');
  const pwd = document.getElementById('password-input2');
  if (!eye || !pwd) return;
  eye.addEventListener('click', () => {
    const tipo = pwd.type === 'password' ? 'text' : 'password';
    pwd.type = tipo;
    eye.classList.toggle('fa-eye-slash');
  });
});
});