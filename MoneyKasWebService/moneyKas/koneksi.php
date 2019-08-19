<?php

  define('HOST', 'localhost');
  define('USER', 'root');
  define('PASS', '');
  define('DB',   'moneyKas');

 // untuk koneksi
  $conn = mysqli_connect(HOST, USER, PASS, DB) or die('tidak bisa konek');

?>
