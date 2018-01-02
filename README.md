# JJJTP
FTPでファイルを取得する

## 使い方
接続先はsetting.xml に記載する。

<getlog>
	ルート要素

<savefolder>
	デフォルトのファイルダウンロード先

<filetypes>
	ファイルの種類を複数定義可能

	<group>
		ファイルグループ名を指定
		<filetype>
			以下の要素を指定する。

        <type>	ファイル種類表示名
        <directory>	ファイルの存在するディレクトリのフルパス。
        <keyword>	任意。デフォルトは”.*”。ファイル検索条件を正規表現で指定する。<directory>以下を検索する。
        <depth>	任意。デフォルトは1。<directory>以下を再帰的に検索する階層の深さを指定する。

<envs>
    環境名を複数定義可能
    <env>
        環境名を属性で指定する
			<machine>
				サーバの情報を定義する。

            <name>	サーバ表示名
            <ip>	サーバのIPアドレス
            <user>	接続ユーザ
            <password>	接続パスワード
            <embed>	任意。埋め込み文字列を設定する
            <group>	ファイルのグループを指定する。filetypes/group に一致するファイル群を表示する。
