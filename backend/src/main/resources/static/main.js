const firebaseConfig = {
  apiKey: "AIzaSyAms04MGhZIdc52FiP2mJGFIiYGxpeOcX8",
  authDomain: "noxshop-90b79.firebaseapp.com",
  projectId: "noxshop-90b79",
};

firebase.initializeApp(firebaseConfig);

const loginBtn = document.getElementById("loginBtn");
const logoutBtn = document.getElementById("logoutBtn");
const productList = document.getElementById("productList");
const purchaseList = document.getElementById("purchaseList");
const userEmail = document.getElementById("userEmail");

loginBtn.addEventListener("click", () => {
  console.log("Login button clicked");
  const provider = new firebase.auth.GoogleAuthProvider();
  firebase.auth().signInWithPopup(provider);
});

logoutBtn.addEventListener("click", () => {
  firebase.auth().signOut();
});

firebase.auth().onAuthStateChanged(async user => {
  if (user) {
    userEmail.textContent = user.email;
    loginBtn.style.display = "none";
    logoutBtn.style.display = "inline-block";

    const token = await user.getIdToken();
    loadProducts(token);
    loadPurchases();
  } else {
    userEmail.textContent = "";
    loginBtn.style.display = "inline-block";
    logoutBtn.style.display = "none";
    productList.innerHTML = "";
    purchaseList.innerHTML = "";
  }
});

async function loadProducts(token) {
  const res = await fetch("/api/products");
  const products = await res.json();

  productList.innerHTML = "";
  products.forEach(product => {
    const card = document.createElement("div");
    card.className = "mdl-card mdl-shadow--2dp product-card";
    card.innerHTML = `
      <div class="mdl-card__title" style="
          background-image: url('https://picsum.photos/120/120');
          background-size: cover;
          background-position: center;
          height: 150px;
          position: relative;
          color: white;
          ">
        <h2 class="mdl-card__title-text" style="
            position: absolute;
            bottom: 8px;
            left: 16px;
            text-shadow: 0 0 5px rgba(0,0,0,0.7);
            margin: 0;
          ">
          ${product.name}
        </h2>
      </div>
      <div class="mdl-card__supporting-text">
        Price: ${product.price.toFixed(2)} kr.
      </div>
      <div class="mdl-card__actions mdl-card--border">
        <button class="mdl-button mdl-js-button mdl-button--accent" onclick="buyProduct(${product.id})">
          Buy
        </button>
      </div>
    `;
    productList.appendChild(card);
    componentHandler.upgradeElement(card); // Required for MDL dynamic elements
  });
}

async function buyProduct(productId) {
  if (!confirm("Are you sure you want to buy this product?")) return;

  const user = firebase.auth().currentUser;
  const token = await user.getIdToken();

  const res = await fetch(`/api/buy?productId=${productId}`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  });

  if (res.ok) {
    alert("Purchase successful!");
    loadPurchases();
  } else {
    alert("Purchase failed.");
  }
}

async function loadPurchases() {
  const user = firebase.auth().currentUser;
  if (!user) {
    console.warn("User not signed in yet.");
    return;
  }

  const token = await user.getIdToken();

  const res = await fetch("/api/purchases", {
    headers: {
      Authorization: "Bearer " + token,
    },
  });

  if (!res.ok) {
    console.error("Failed to fetch purchases", res.status);
    return;
  }

  const purchases = await res.json();
  console.log("Loaded purchases:", purchases);

  const purchaseList = document.getElementById("purchaseList");
  purchaseList.innerHTML = ""; // Clear existing

  purchases.forEach((purchase) => {
    const li = document.createElement("li");
    li.className = "mdl-list__item";

    const timestamp = purchase.timestamp ? new Date(purchase.timestamp).toLocaleString() : "Unknown time";

    li.innerHTML = `
      <span class="mdl-list__item-primary-content">
        <strong>Product ID: ${purchase.productName}</strong> – ${purchase.price} kr.
      </span>
      <span class="mdl-list__item-secondary-content">
        <small>${timestamp}</small>
      </span>
    `;

    purchaseList.appendChild(li);
  });
}
