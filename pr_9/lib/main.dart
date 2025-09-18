import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class Product {
  final String name;
  final String imageUrl;
  final double price;

  Product({required this.name, required this.imageUrl, required this.price});
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Product Catalog',
      theme: ThemeData(
        primarySwatch: Colors.teal,
        // colorScheme: ColorScheme.fromSeed(seedColor: Colors.teal), // Alternative theming
        useMaterial3: true,
      ),
      home: const ProductCatalogScreen(),
    );
  }
}

class ProductCatalogScreen extends StatefulWidget {
  const ProductCatalogScreen({super.key});

  @override
  State<ProductCatalogScreen> createState() => _ProductCatalogScreenState();
}

class _ProductCatalogScreenState extends State<ProductCatalogScreen> {
  final List<Product> _products = [
    Product(name: 'Laptop Pro', imageUrl: 'https://via.placeholder.com/150/0000FF/808080?Text=Laptop', price: 999.99),
    Product(name: 'Wireless Mouse', imageUrl: 'https://via.placeholder.com/150/FF0000/FFFFFF?Text=Mouse', price: 25.50),
    Product(name: 'Keyboard RGB', imageUrl: 'https://via.placeholder.com/150/00FF00/000000?Text=Keyboard', price: 75.00),
    Product(name: 'HD Monitor', imageUrl: 'https://via.placeholder.com/150/FFFF00/000000?Text=Monitor', price: 199.99),
    Product(name: 'Webcam 1080p', imageUrl: 'https://via.placeholder.com/150/00FFFF/000000?Text=Webcam', price: 59.90),
    Product(name: 'Gaming Headset', imageUrl: 'https://via.placeholder.com/150/FF00FF/FFFFFF?Text=Headset', price: 89.75),
    Product(name: 'Smartphone X', imageUrl: 'https://via.placeholder.com/150/C0C0C0/000000?Text=Phone', price: 699.00),
    Product(name: 'Tablet Pro', imageUrl: 'https://via.placeholder.com/150/808000/FFFFFF?Text=Tablet', price: 320.00),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Product Catalog'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
      body: Padding(
        padding: const EdgeInsets.all(8.0),
        child: GridView.builder(
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 2, // Number of columns
            crossAxisSpacing: 10.0, // Horizontal space between cards
            mainAxisSpacing: 10.0, // Vertical space between cards
            childAspectRatio: 0.75, // Aspect ratio of the cards (width / height)
          ),
          itemCount: _products.length,
          itemBuilder: (context, index) {
            return ProductCard(product: _products[index]);
          },
        ),
      ),
    );
  }
}

class ProductCard extends StatelessWidget {
  final Product product;

  const ProductCard({super.key, required this.product});

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 4.0,
      clipBehavior: Clip.antiAlias, // Ensures the image corners are rounded if the card is
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10.0),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          Expanded(
            child: Image.network(
              product.imageUrl,
              fit: BoxFit.cover,
              // Basic error handling for images
              errorBuilder: (context, error, stackTrace) {
                return const Center(child: Icon(Icons.broken_image, size: 50, color: Colors.grey));
              },
              // Loading indicator for images
              loadingBuilder: (BuildContext context, Widget child, ImageChunkEvent? loadingProgress) {
                if (loadingProgress == null) return child;
                return Center(
                  child: CircularProgressIndicator(
                    value: loadingProgress.expectedTotalBytes != null
                        ? loadingProgress.cumulativeBytesLoaded / loadingProgress.expectedTotalBytes!
                        : null,
                  ),
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Text(
              product.name,
              style: const TextStyle(
                fontSize: 16.0,
                fontWeight: FontWeight.bold,
              ),
              textAlign: TextAlign.center,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
            ),
          ),
          Padding(
            padding: const EdgeInsets.only(bottom: 8.0),
            child: Text(
              '\$${product.price.toStringAsFixed(2)}',
              style: TextStyle(
                fontSize: 14.0,
                color: Colors.green[700],
                fontWeight: FontWeight.w600,
              ),
              textAlign: TextAlign.center,
            ),
          ),
        ],
      ),
    );
  }
}
