<?php

  require_once('koneksi.php');

  // untuk membaca data di sql, DESC di ambil dari belakang
  $sql = mysqli_query($conn, "SELECT * FROM TRANSAKSI ORDER BY transaksi_id DESC");

    // jika berhasil membaca data dari sql
    // maka hasil pembacaan datanya dimasukkan ke dalam array hasil
    $hasil = array();
    while ($baris = mysqli_fetch_array($sql)) {

      $date = date_create( $baris['tanggal']);
      $tanggal = date_format( $date, "d/m/Y");

      array_push($hasil, array(
        'transaksi_id'  => $baris['transaksi_id'],
        'status'        => $baris['status'],
        'jumlah'        => $baris['jumlah'],
        'keterangan'    => $baris['keterangan'],
        'tanggal'       => $tanggal,
'tanggal2'       => $baris['tanggal'],
      ));
    }

    // menampilkan jumlah uang dari sql masuk dan keluar
    $sql = mysqli_query($conn,"SELECT (SELECT SUM(jumlah) from transaksi WHERE status='masuk') AS masuk,
    (SELECT SUM(jumlah) from transaksi WHERE status='keluar') AS keluar" );

    while ($baris = mysqli_fetch_array($sql)) {
    $masuk  = $baris['masuk'];
    $keluar  = $baris['keluar'];
    }

    echo json_encode(array(
      'masuk'    => $masuk,
      'keluar'   => $keluar,
      'saldo'   => ($masuk - $keluar),
      'hasil'    => $hasil ));

?>
